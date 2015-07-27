package com.scalakata

import autowire._
import org.denigma.codemirror._
import org.scalajs.dom
import org.scalajs.dom.navigator
import org.scalajs.dom.raw.{HTMLTextAreaElement, HTMLElement, Node}
import scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scalajs.js
import scalajs.js.annotation.JSExport
import scalatags.JsDom.all._

@JSExport
object Main {
  @JSExport
  def main(): Unit = {

    val ctrl =
      if(navigator.userAgent.contains("Mac")) "Cmd"
      else "Ctrl"

    val modeScala = "text/x-scala"


    val params = EditorConfig.
      mode(modeScala).
      lineNumbers(false).
      lineWrapping(false).
      tabSize(2).
      indentWithTabs(false).
      theme("solarized dark").
      smartIndent(true).
      keyMap("sublime").
      extraKeys(js.Dictionary(
        s"$ctrl-Space" -> "autocomplete",
         "."           -> "autocompleteDot",
        s"$ctrl-Enter" -> "run",
        s"$ctrl-,"     -> "config",
        s"$ctrl-."     -> "typeAt",
         "Tab"         -> "insertSoftTab"
      )).
      autoCloseBrackets(true).
      matchBrackets(true).
      showCursorWhenSelecting(true).
      autofocus(true).
      highlightSelectionMatches(js.Dictionary(
        "showToken" -> js.Dynamic.global.RegExp("\\w")
      ))
    
    val nl = "\n"
    val prelude = 
      """|import com.scalakata._
         |@instrument class A {
         |  """.stripMargin
    val default = prelude + nl + "}"

    dom.document.getElementById("scalakata") match {
      case el:HTMLTextAreaElement ⇒
        val editor = CodeMirror.fromTextArea(el, params)
        val doc = editor.getDoc()

        var annotations = List.empty[Anoted]        
        val converter = Pagedown.getSanitizingConverter()

        def resetDefault(): Unit = {
          if(doc.getValue().isEmpty) {
            doc.setValue(default)
            doc.setCursor(doc.posFromIndex(prelude.length))
          }
        }

        def clear(): Unit = {
          annotations.foreach(_.clear())
        }

        editor.on("change", (_, _) ⇒ {
          resetDefault()
          clear()
        })

        sealed trait Anoted { def clear(): Unit }
        case class Line(lw: LineWidget) extends Anoted { def clear() = lw.clear() }
        case class Marked(tm: TextMarker) extends Anoted { def clear() = tm.clear() }
        case class Widget(node: HTMLElement) extends Anoted { def clear() = {node.parentNode.removeChild(node); ()} }
        case object Empty extends Anoted { def clear() = () }

        val run = { () ⇒
          val request = EvalRequest(doc.getValue(nl))
          Client[Api].eval(request).call().onSuccess{ case response ⇒
            clear()

            // <span class="oi" data-glyph="timer"></span>

            def noop[T](v: T): Unit = ()

            def nextline2(endPos: Position, node: HTMLElement, process: (HTMLElement => Unit) = noop, options: js.Any = null): Anoted = {
              process(node)
              Line(editor.addLineWidget(endPos.line, node, options))
            }

            def nextline(endPos: Position, content: String, process: (HTMLElement => Unit) = noop, options: js.Any = null): Anoted = {
              val node = pre(content).render
              nextline2(endPos, node, process, options)
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

                def fold(content: String, process: (HTMLElement => Unit) = noop): Anoted = {
                  val node = pre(`class` := "fold")(content).render
                  process(node)
                  Marked(doc.markText(startPos, endPos, TextMarkerConfig.replacedWith(node)))
                }
                def inline(content: String, process: (HTMLElement => Unit) = noop): Anoted = {
                  val node = pre(`class` := "inline")(content).render
                  process(node)
                  startPos.ch = doc.getLine(startPos.line).length
                  Marked(doc.setBookmark(startPos, js.Dictionary(
                    "widget" -> node
                  )))
                }
                
                repr match {
                  case EString(v) ⇒ {
                    if(v.contains(nl)) nextline(endPos, v)
                    else inline(v)
                  }
                  case Other(v) ⇒ inline(v, {
                    node => CodeMirror.runMode(v, modeScala, node)
                    ()
                  })
                  case Markdown(v, folded) ⇒ {
                    val process: (HTMLElement => Unit) = _.innerHTML = converter.makeHtml(v)
                    if(!folded) nextline(endPos, v, process)
                    else fold(v, process)
                  }
                  case Html(v, folded) ⇒ {
                    val process: (HTMLElement => Unit) = _.innerHTML = v
                    if(!folded) nextline(endPos, v, process)
                    else fold(v, process)
                  }
                }
              }

            annotations = timeout ::: runtimeError ::: instrumentations ::: complilationInfos
          }
        }
        CodeMirror.commands.run = run



        resetDefault()
        run()
      case _ ⇒ dom.console.error("cannot find text area for the code!")
    }
  }
}

// dom.console.log(response.toString)

// editor.addWidget(pos: {line, ch}, node: Element, scrollIntoView: boolean)
// editor.addLineWidget(line: Int, node: HTMLElement, options: js.Any = js.native): LineWidget = js.native
// editor.markText({ch: 0, line: start.line}, end, { replacedWith: e})