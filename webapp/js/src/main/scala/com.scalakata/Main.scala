package com.scalakata

import autowire._
import org.denigma.codemirror._
import org.scalajs.dom
import org.scalajs.dom.navigator
import org.scalajs.dom.raw.HTMLTextAreaElement
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
      lineWrapping(true).
      tabSize(2).
      theme("mdn-like").
      smartIndent(true).
      keyMap("sublime").
      extraKeys(js.Dictionary(
        s"$ctrl-Space" -> "autocomplete",
         "."           -> "autocompleteDot",
        s"$ctrl-Enter" -> "run",        s"$ctrl-,"     -> "config",
        s"$ctrl-."     -> "typeAt"
      )).
      autoCloseBrackets(true).
      matchBrackets(true).
      showCursorWhenSelecting(true).
      highlightSelectionMatches(js.Dictionary(
        "showToken" -> js.Dynamic.global.RegExp("\\w")
      ))
    
    val default = 
      """|import com.scalakata._
         |@instrument class A {
         |  Markdown((1 to 5).map(i => "#" * i + s" Header $i").mkString(System.lineSeparator))
         |}""".stripMargin

    val nl = "\n"

    dom.document.getElementById("scalakata") match {
      case el:HTMLTextAreaElement ⇒
        val editor = CodeMirror.fromTextArea(el, params)
        editor.getDoc.setValue(default)

        var insights = List.empty[LineWidget]        
        val converter = Pagedown.getSanitizingConverter()

        val run = { () ⇒
          val request = EvalRequest(editor.getDoc.getValue(nl))
          Client[Api].eval(request).call().map{ response ⇒
            
            response.instrumentation.map{ case (RangePosition(start, _, end), repr) ⇒
              val endPos = editor.getDoc.posFromIndex(end)
              repr match {
                case EString(v) ⇒ {
                  if(v.contains(nl)) editor.addLineWidget(endPos.line, pre(v).render)
                  else () // TODO: next to
                }
                case Other(v) ⇒ {
                  if(v.contains(nl)) {
                    val out = pre().render
                    CodeMirror.runMode(v, modeScala, out)
                    editor.addLineWidget(endPos.line, out)
                  } else () // TODO: next to
                }
                case Markdown(v, folded) ⇒ {
                  dom.console.log(s"md $v")
                  val out = pre().render
                  out.innerHTML = converter.makeHtml(v)
                  dom.console.log(out)
                  if(!folded) {
                    editor.addLineWidget(endPos.line, out)
                  } else {
                    // editor.markText
                  }
                }
                case Html(v, folded) ⇒ {
                  dom.console.log(s"html $v")
                  if(!folded) {

                  } else {
                    
                  }
                }
              }
            }
            ()
          }
          ()
        }
        CodeMirror.commands.run = run
        run()
      case _ ⇒ dom.console.error("cannot find text area for the code!")
    }
  }
}
