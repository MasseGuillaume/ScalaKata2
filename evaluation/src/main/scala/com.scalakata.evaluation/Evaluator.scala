package com.scalakata
package evaluation

import scala.tools.nsc.Global
import scala.tools.nsc.reporters.StoreReporter
import scala.tools.nsc.io.{VirtualDirectory, AbstractFile}
import scala.reflect.internal.util.{NoPosition, BatchSourceFile, AbstractFileClassLoader}

import java.io.File
import java.nio.file.Path
import java.net.URLClassLoader
import java.util.concurrent.{TimeoutException, Callable, FutureTask, TimeUnit}

import scala.util.Try
import scala.util.control.NonFatal
import scala.concurrent.duration._

class Evaluator(artifacts: Seq[Path], scalacOptions: Seq[String], security: Boolean, timeout: Duration) {

  def apply(request: EvalRequest): EvalResponse = synchronized {
    if (request.code.isEmpty) EvalResponse.empty
    else {
      try { runTimeout(request.code)
      } catch { case NonFatal(e) ⇒ handleException(e) }
    }
  }

  private val secured = new Secured(security)

  private def eval(code: String): EvalResponse = {
    secured { compile(code) }
    val infos = check()
    if(!infos.contains(Error)) {
      // Look for static class implementing Instrumented
      def findEval: Option[(Instrumentation, String)] = {
        def removeExt(of: String) = {
          val classExt = ".class"
          if(of.endsWith(classExt)) of.slice(0, of.lastIndexOf(classExt))
          else of
        }

        def removeMem(of: String) = {
          of.slice("(memory)/".length, of.length)
        }

        def recurseFolders(file: AbstractFile): Set[AbstractFile] = {
          file.iterator.to[Set].flatMap{ fs ⇒
            if(fs.isDirectory)
              fs.to[Set] ++
              fs.filter(_.isDirectory).flatMap(recurseFolders).to[Set]
            else Set(fs)
          }
        }

        val instrClass =
          recurseFolders(target).
          map(_.path).
          map(((removeExt _) compose (removeMem _))).
          map(_.replace('/', '.')).
          filterNot(_.endsWith("$class")).
          find { n ⇒
            Try(classLoader.loadClass(n)).map(
              _.getInterfaces.exists(_ == classOf[Instrumented])
            ).getOrElse(false)
          }
        
        instrClass.map{ c ⇒
          val cl = Class.forName(c, false, classLoader)
          val cons = cl.getConstructor()
          secured {
            val baos = new java.io.ByteArrayOutputStream()
            val ps = new java.io.PrintStream(baos)
            val result = Console.withOut(ps)(cons.newInstance().asInstanceOf[Instrumented].instrumentation$)
            (result, baos.toString("UTF-8"))
          }
        }
      }

      val (instrumentation, console) = findEval.getOrElse((Nil, ""))
      EvalResponse.empty.copy(
        instrumentation = instrumentation,
        console = console,
        complilationInfos = infos
      )
    } else {
      EvalResponse.empty.copy(complilationInfos = infos)
    }
  }

  private def runTimeout(code: String) =
    withTimeout{eval(code)}(timeout).getOrElse(EvalResponse.empty.copy(timeout = true))

  private def withTimeout[T](f: ⇒ T)(timeout: Duration): Option[T] = {
    val task = new FutureTask(new Callable[T]() { def call = f })
    val thread = new Thread(task)
    try {
      thread.start()
      Some(task.get(timeout.toMillis, TimeUnit.MILLISECONDS))
    } catch {
      case e: TimeoutException ⇒ None
    } finally {
      if(thread.isAlive) thread.stop()
    }
  }

  private def handleException(e: Throwable): EvalResponse = {
    def search(e: Throwable) = {
      e.getStackTrace.find(_.getFileName == "(inline)").map(v ⇒ 
        (e, Some(v.getLineNumber))
      )
    }
    def loop(e: Throwable): Option[(Throwable, Option[Int])] = {
      val s = search(e)
      if(s.isEmpty)
        if(e.getCause != null) loop(e.getCause)
        else Some((e, None))
      else s
    }
    EvalResponse.empty.copy(runtimeError = loop(e).map{ case (err, line) ⇒
      RuntimeError(err.toString, line)
    })
  }

  private def check(): Map[Severity, List[CompilationInfo]] = {
    val infos =
      reporter.infos.map { info ⇒
        val pos = info.pos match {
          case NoPosition ⇒ None
          case _ ⇒ Some(RangePosition(info.pos.start, info.pos.point, info.pos.end))
        }
        (
          info.severity,
          info.msg,
          pos
        )
      }.to[List]
       .filterNot{ case (sev, msg, _) ⇒
        // annoying
        sev == reporter.WARNING &&
        msg == ("a pure expression does nothing in statement " +
                "position; you may be omitting necessary parentheses")
      }.groupBy(_._1)
       .mapValues{_.map{case (_ ,msg, pos) ⇒ (msg, pos)}}

    def convert(infos: Map[reporter.Severity, List[(String, Option[RangePosition])]]): Map[Severity, List[CompilationInfo]] = {
      infos.map{ case (k,vs) ⇒
        val sev = k match {
          case reporter.ERROR ⇒ Error
          case reporter.WARNING ⇒ Warning
          case reporter.INFO ⇒ Info
        }
        val info = vs map {case (msg, pos) ⇒
          CompilationInfo(msg, pos)
        }
        (sev, info)
      }
    }
    convert(infos)
  }

  private def reset(): Unit = {
    target.clear()
    reporter.reset()
    classLoader = new AbstractFileClassLoader(target, artifactLoader)
  }

  private def compile(code: String): Unit = {
    reset()
    val run = new compiler.Run
    val sourceFiles = List(new BatchSourceFile("(inline)", code))
    run.compileSources(sourceFiles)
  }
  private val reporter = new StoreReporter()
  private val settings = toSettings(artifacts, scalacOptions)
  private val artifactLoader = {
    val loaderFiles =
      settings.classpath.value.split(File.pathSeparator).map(a ⇒ {

        val node = new java.io.File(a)
        val endSlashed =
          if(node.isDirectory) node.toString + File.separator
          else node.toString

        new File(endSlashed).toURI().toURL
      })
    new URLClassLoader(loaderFiles, this.getClass.getClassLoader)
  }
  private val target = new VirtualDirectory("(memory)", None)
  private var classLoader: AbstractFileClassLoader = _
  settings.outputDirs.setSingleOutput(target)
  private val compiler = new Global(settings, reporter)
}