package com.scalakata

import autowire._
import org.denigma.codemirror._
import org.scalajs.dom
import org.scalajs.dom.navigator
import org.scalajs.dom.raw.HTMLElement
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import scalajs.js
import scalatags.JsDom.all._
import scala.concurrent.Future
import org.scalajs.dom.KeyboardEvent

object Rendering {
  import Util._
  var toclear = false
  val modeScala = "text/x-scala"
  val stateButton = dom.document.getElementById("state")
  val isMac = navigator.userAgent.contains("Mac")
  val ctrlS = if(isMac) "⌘" else "Ctrl"

  def clear(doc: Doc): Unit = {
    stateButton.setAttribute("data-glyph", "media-play")
    stateButton.setAttribute("title", s"run ($ctrlS + Enter)")
    annotations.foreach(_.clear())
    doc.setCursor(doc.getCursor())
  }

  def resetCursor(doc: Doc): Unit = {
    doc.setCursor(doc.posFromIndex(prelude.length))
  }

  def run(editor: Editor) = {
    val doc = editor.getDoc()
    stateButton.setAttribute("data-glyph", "clock")
    stateButton.setAttribute("title", "evaluating ...")

    def resetDefault(): Unit = {
      if(doc.getValue().isEmpty) {
        doc.setValue(wrap(""))
        resetCursor(doc)
      }
    }

    editor.on("keyup", (_, event) ⇒ {
      val ev = event.asInstanceOf[KeyboardEvent]
      val esc = 27
      if(ev.keyCode == esc) {
        toclear = false
        clear(doc)
      }
    })

    editor.on("change", (_, _) ⇒ {
      dom.localStorage.setItem(localStorageKey, doc.getValue())
      resetDefault()
    })
    resetDefault()

    def noop[T](v: T): Unit = ()

    def nextline2(endPos: Position, node: HTMLElement, process: (HTMLElement ⇒ Unit) = noop, options: js.Any = null): Anoted = {
      process(node)
      Line(editor.addLineWidget(endPos.line, node, options))
    }

    def nextline(endPos: Position, content: String, process: (HTMLElement ⇒ Unit) = noop, options: js.Any = null): Anoted = {
      val node = pre(`class` := "line")(content).render
      nextline2(endPos, node, process, options)
    }

    def fold(startPos: Position, endPos: Position, content: String, process: (HTMLElement ⇒ Unit) = noop): Anoted = {
      val node = div(`class` := "fold")(content).render
      process(node)
      Marked(doc.markText(startPos, endPos, TextMarkerConfig.replacedWith(node)))
    }
    def inline(startPos: Position, content: String, process: (HTMLElement ⇒ Unit) = noop): Anoted = {
      // inspired by blink/devtools WebInspector.JavaScriptSourceFrame::_renderDecorations
      val basePos = Pos.line(startPos.line).ch(0)
      val offsetPos = Pos.line(startPos.line).ch(doc.getLine(startPos.line).length)

      val mode = "local"
      val base = editor.cursorCoords(basePos, mode)
      val offset = editor.cursorCoords(offsetPos, mode)

      val node = pre(`class` := "inline", left := offset.left - base.left)(content).render
      process(node)

      Line(editor.addLineWidget(startPos.line, node))
    }

    def doRender(startPos: Position, endPos: Position, render: Render): Anoted = render match {
      case Value(v, tpe) ⇒ {
        val process = (node: HTMLElement) ⇒ {
          CodeMirror.runMode(s"$v: $tpe", modeScala, node)
          node.title = tpe
          ()
        }
        if(v.contains(nl)) nextline(endPos, v, process)
        else inline(startPos, v, process)
      }
      case Markdown(v, folded) ⇒ {

        def process(elem: HTMLElement): Unit = {
          val escaper = span().render
          val content = converter.makeHtml(v)

          import scala.scalajs.js.JSStringOps._
          // js regex only replace once
          def fix(f: String ⇒ String)(v0: String) = {
            def itt(v: String): String = {
              val t = f(v)
              if(t == v) t
              else itt(t)
            }
            itt(v0)
          }

          elem.innerHTML =
            fix{ v ⇒
              v.jsReplace(RegexHelper.codeReg, (b: String, c: String) ⇒ {
                val node =
                  if(c.contains(nl)) pre(`class` := "code block").render
                  else span(`class` := "code short").render
                escaper.innerHTML = c
                CodeMirror.runMode(escaper.textContent, modeScala, node)
                node.outerHTML
              })
            }(content)
        }
        if(!folded) nextline(endPos, v, process)
        else fold(startPos, endPos, v, process)
      }
      case Html(v, folded) ⇒ {
        val process: (HTMLElement ⇒ Unit) = _.innerHTML = v
        if(!folded) nextline(endPos, v, process)
        else fold(startPos, endPos, v, process)
      }
      case Html2(v, folded) ⇒ {

        val frameId = s"frame-${java.util.UUID.randomUUID().toString()}"

        val echoForm =
          form(action := "/echo", target := frameId, method := "post")(
            input(name := "code", value := v, `type` := "hidden")
          ).render

        val echoFrame =
          div(
            iframe(
              name := frameId,
              "scrolling".attr :="no",
              "allow-top-navigation".attr := "true",
              "allow-popups".attr := "true",
              "allowTransparency".attr := "true",
              style := "width: 100%"
            ),
            script("iFrameResize({'checkOrigin': false, 'heightCalculationMethod': 'min'})")
          ).render

        dom.setTimeout( () ⇒ {
          echoForm.submit()
        }, 0)

        val process: (HTMLElement ⇒ Unit) = {
          e ⇒ e.appendChild(echoFrame)
          ()
        }
        if(!folded) nextline(endPos, "", process)
        else fold(startPos, endPos, "", process)
      }
    }

    Client[Api].eval(EvalRequest(doc.getValue())).call().onSuccess{ case response ⇒
      clear(doc)
      toclear = true
      stateButton.setAttribute("data-glyph", "circle-x")
      stateButton.setAttribute("title", s"clear (Esc)")

      val complilationInfos = {
        for {
          (severity, infos) <- response.complilationInfos
          info <- infos
        } yield {
          def severityToIcon(sev: Severity) = sev match {
            case Info ⇒ "info"
            case Warning ⇒ "warning"
            case Error ⇒ "circle-x"
          }
          val sev = severity.toString.toLowerCase

          info.pos match {
            case None ⇒ {
              val node = div(`class` := s"compiler $sev")(
                i(`class`:="oi", "data-glyph".attr := severityToIcon(severity)),
                span(info.message)
              ).render
              Line(editor.addLineWidget(doc.firstLine(), node))
            }
            case Some(RangePosition(start, _, end)) ⇒ {
              val startPos = doc.posFromIndex(start)

              val childs =
                List(
                  span(" " * startPos.ch),
                  span("^"),
                  i(`class`:="oi", "data-glyph".attr := severityToIcon(severity)),
                  pre(info.message)
                )

              val node = pre(`class` := s"compiler $sev")(span(childs: _*)).render
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
        response.runtimeError.map{ case RuntimeError(message, pos) ⇒
          val node = div(`class` := "runtime-error")(
            i(`class`:="oi", "data-glyph".attr := "circle-x"),
            span(message)
          ).render
          nextline2(Pos.ch(0).line(pos.map(_ - 1).getOrElse(0)), node)
        }.toList

      def line(instr: (RangePosition, Render)) = {
        val (RangePosition(start, _, _), _) = instr
        doc.posFromIndex(start).line
      }

      val instrumentations: List[Anoted] =
        response.instrumentation.
          groupBy(line).
          values.flatMap{ renders ⇒
            // join single line value
            if(renders.forall{case (_, Value(v, t)) ⇒ !v.contains(nl); case _ ⇒ false}) {
              val vs = renders.map{case (_, Value(v, _)) ⇒ v; case _ ⇒ ""}
              val tpes = renders.map{case (_, Value(_, t)) ⇒ t; case _ ⇒ ""}

              val joined =
                if(vs.size == 1 && tpes.size == 1) Value(vs.head, tpes.head)
                else Value(vs.mkString("(", ", ", ")"), tpes.mkString("(", ", ", ")"))

              List((renders.head._1, joined))
            }
            else renders
          }.map{case ((RangePosition(start, _, end), render)) ⇒
            val startPos = doc.posFromIndex(start)
            val endPos = doc.posFromIndex(end)

            doRender(startPos, endPos, render)
          }.
          toList

      annotations = timeout ::: runtimeError ::: instrumentations ::: complilationInfos
      editor.scrollIntoView(doc.getCursor(), dom.screen.height/2)
    }
  }

  private sealed trait Anoted { def clear(): Unit }
  private case class Line(lw: LineWidget) extends Anoted { def clear() = lw.clear() }
  private case class Marked(tm: TextMarker) extends Anoted { def clear() = tm.clear() }
  private case class Widget(node: HTMLElement) extends Anoted { def clear() = {node.parentNode.removeChild(node); ()} }
  private case object Empty extends Anoted { def clear() = () }

  private var annotations = List.empty[Anoted]
  private val converter = Pagedown.getSanitizingConverter()

  private val nl = '\n'
  private val prelude =
    """|import com.scalakata._
       |
       |@instrument class Playground {
       |  """.stripMargin

  def wrap(code: String): String = prelude + code + nl + "}"

  val localStorageKey = "code"
}
