package com.scalakata

import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.reporters.StoreReporter
import scala.tools.nsc.io.{VirtualDirectory, AbstractFile}
import scala.reflect.internal.util.{NoPosition, BatchSourceFile, AbstractFileClassLoader}

import scala.util.Try
import scala.language.reflectiveCalls

import java.io.File
import java.net.{URL, URLClassLoader}

class Eval(settings: Settings, security: Boolean) {
  val secured = new Secured(security)
  def apply(code: String): EvalResponse = {
    secured { compile(code) }
    val infos = check()
    if(!infos.contains(Error)) {
      // Look for static class implementing Instrumented
      def findEval: Option[Instrumentation] = {
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
            cons.newInstance().asInstanceOf[Instrumented].instrumentation$
          }
        }
      }

      EvalResponse.empty.copy(
        instrumentation = findEval.getOrElse(Nil),
        complilationInfos = infos
      )
    } else {
      EvalResponse.empty.copy(complilationInfos = infos)
    }
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