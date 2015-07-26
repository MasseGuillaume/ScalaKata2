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
      theme("solarized dark").
      smartIndent(true).
      keyMap("sublime").
      extraKeys(js.Dictionary(
        s"$ctrl-Space" -> "autocomplete",
         "."           -> "autocompleteDot",
        s"$ctrl-Enter" -> "run",
        s"$ctrl-,"     -> "config",
        s"$ctrl-."     -> "typeAt"
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

        var insights = List.empty[Anoted]        
        val converter = Pagedown.getSanitizingConverter()

        def resetDefault(): Unit = {
          if(doc.getValue().isEmpty) {
            doc.setValue(default)
            doc.setCursor(doc.posFromIndex(prelude.length))
          }
        }

        def clear(): Unit = {
          insights.foreach(_.clear())
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
            insights = 
              response.instrumentation.map{ case (RangePosition(start, _, end), repr) ⇒
                val startPos = doc.posFromIndex(start)
                val endPos = doc.posFromIndex(end)

                def noop[T](v: T): Unit = ()

                def inline(content: String, process: (HTMLElement => Unit) = noop): Anoted = {
                  val node = pre(`class` := "inline")(content).render
                  process(node)
                  startPos.ch = doc.getLine(startPos.line).length
                  Marked(doc.setBookmark(startPos, js.Dictionary(
                    "widget" -> node
                  )))
                }
                def fold(content: String, process: (HTMLElement => Unit) = noop): Anoted = {
                  val node = pre(`class` := "fold")(content).render
                  process(node)
                  Marked(doc.markText(startPos, endPos, TextMarkerConfig.replacedWith(node)))
                }
                def nextline(content: String, process: (HTMLElement => Unit) = noop): Anoted = {
                  val node = pre(content).render
                  process(node)
                  Line(editor.addLineWidget(endPos.line, node))
                }

                repr match {
                  case EString(v) ⇒ {
                    if(v.contains(nl)) nextline(v)
                    else inline(v)
                  }
                  case Other(v) ⇒ inline(v, {
                    node => CodeMirror.runMode(v, modeScala, node)
                    ()
                  })
                  case Markdown(v, folded) ⇒ {
                    val process: (HTMLElement => Unit) = _.innerHTML = converter.makeHtml(v)
                    if(!folded) nextline(v, process)
                    else fold(v, process)
                  }
                  case Html(v, folded) ⇒ {
                    val process: (HTMLElement => Unit) = _.innerHTML = v
                    if(!folded) nextline(v, process)
                    else fold(v, process)
                  }
                }
              }
          }
        }
        CodeMirror.commands.run = run

        resetDefault()
        run()
      case _ ⇒ dom.console.error("cannot find text area for the code!")
    }
  }
}


// for {
//   (severity, infos) <- response.complilationInfos
//   info <- infos
// } yield {
//   info.pos
// }

// dom.console.log(response.toString)

// editor.addWidget(pos: {line, ch}, node: Element, scrollIntoView: boolean)
// editor.addLineWidget(line: Int, node: HTMLElement, options: js.Any = js.native): LineWidget = js.native
// editor.markText({ch: 0, line: start.line}, end, { replacedWith: e})