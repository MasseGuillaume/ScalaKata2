package com.scalakata

import autowire._
import org.denigma.codemirror._
import org.scalajs.dom.raw.HTMLElement
import scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scalajs.js
import scalatags.JsDom.all._
import scala.concurrent.Future


object Rendering {
  val modeScala = "text/x-scala"

  def run(editor: Editor) = {
    val doc = editor.getDoc()
    def resetDefault(): Unit = {
      if(doc.getValue().isEmpty) {
        doc.setValue(default)
        doc.setCursor(doc.posFromIndex(prelude.length))
      }
    }
    def clear(): Unit = annotations.foreach(_.clear())

    editor.on("change", (_, _) ⇒ {
      resetDefault()
      clear()
    })
   
    resetDefault()

    val request = EvalRequest(doc.getValue(nl))
    Client[Api].eval(request).call().onSuccess{ case response ⇒
      clear()

      def noop[T](v: T): Unit = ()

      def nextline2(endPos: Position, node: HTMLElement, process: (HTMLElement => Unit) = noop, options: js.Any = null): Anoted = {
        process(node)
        Line(editor.addLineWidget(endPos.line, node, options))
      }

      def nextline(endPos: Position, content: String, process: (HTMLElement => Unit) = noop, options: js.Any = null): Anoted = {
        val node = pre(content).render
        nextline2(endPos, node, process, options)
      }

      def fold(startPos: Position, endPos: Position, content: String, process: (HTMLElement => Unit) = noop): Anoted = {
        val node = pre(`class` := "fold")(content).render
        process(node)
        Marked(doc.markText(startPos, endPos, TextMarkerConfig.replacedWith(node)))
      }
      def inline(startPos: Position, content: String, process: (HTMLElement => Unit) = noop): Anoted = {
        val node = pre(`class` := "inline")(content).render
        process(node)
        startPos.ch = doc.getLine(startPos.line).length
        Marked(doc.setBookmark(startPos, js.Dictionary(
          "widget" -> node
        )))
      }

      val complilationInfos = {
        for {
          (severity, infos) <- response.complilationInfos
          info <- infos
        } yield {
          def severityToIcon(sev: Severity) = sev match {
            case Info => "info"
            case Warning => "warning"
            case Error => "circle-x"
          }
          val sev = severity.toString.toLowerCase

          info.pos match {
            case None => {
              val node = div(`class` := s"compiler $sev")(
                i(`class`:="oi", "data-glyph".attr := severityToIcon(severity)),
                span(info.message)
              ).render
              Line(editor.addLineWidget(doc.firstLine(), node))
            }
            case Some(RangePosition(start, _, end)) => {
              val startPos = doc.posFromIndex(start)
              val node = div(`class` := s"compiler $sev")(
                pre(" " * startPos.ch + "^ "),
                i(`class`:="oi", "data-glyph".attr := severityToIcon(severity)),
                pre(info.message)
              ).render
              Line(editor.addLineWidget(startPos.line, node))
            }
          }
        }
      }.toList

      val timeout =
        if(response.timeout) {
          val node = div(`class` := "timeout")(
            i(`class`:="oi", "data-glyph".attr := "timer"),
            span("evaluation timed out")
          ).render
          List(Line(editor.addLineWidget(0, node)))
        } else Nil

      val runtimeError =
        response.runtimeError.map{ case RuntimeError(message, pos) =>
          val node = div(`class` := "runtime-error")(
            i(`class`:="oi", "data-glyph".attr := "circle-x"),
            span(message)
          ).render
          nextline2(Pos.ch(0).line(pos.map(_ - 1).getOrElse(0)), node)
        }.toList

      val instrumentations = 
        response.instrumentation.map{ case (RangePosition(start, _, end), repr) ⇒
          val startPos = doc.posFromIndex(start)
          val endPos = doc.posFromIndex(end)
          repr match {
            case EString(v) ⇒ {
              if(v.contains(nl)) nextline(endPos, v)
              else inline(startPos, v)
            }
            case Other(v) ⇒ inline(startPos, v, {
              node => CodeMirror.runMode(v, modeScala, node)
              ()
            })
            case Markdown(v, folded) ⇒ {
              val process: (HTMLElement => Unit) = _.innerHTML = converter.makeHtml(v)
              if(!folded) nextline(endPos, v, process)
              else fold(startPos, endPos, v, process)
            }
            case Html(v, folded) ⇒ {
              val process: (HTMLElement => Unit) = _.innerHTML = v
              if(!folded) nextline(endPos, v, process)
              else fold(startPos, endPos, v, process)
            }
          }
        }

      annotations = timeout ::: runtimeError ::: instrumentations ::: complilationInfos
    }
  }

  private sealed trait Anoted { def clear(): Unit }
  private case class Line(lw: LineWidget) extends Anoted { def clear() = lw.clear() }
  private case class Marked(tm: TextMarker) extends Anoted { def clear() = tm.clear() }
  private case class Widget(node: HTMLElement) extends Anoted { def clear() = {node.parentNode.removeChild(node); ()} }
  private case object Empty extends Anoted { def clear() = () }

  private var annotations = List.empty[Anoted]        
  private val converter = Pagedown.getSanitizingConverter()

  private val nl = "\n"
  private val prelude = 
    """|import com.scalakata._
       |@instrument class Playground {
       |  """.stripMargin
  private val default = prelude + nl + "}"
}