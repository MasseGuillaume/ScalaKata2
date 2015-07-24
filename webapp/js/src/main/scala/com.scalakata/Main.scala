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

    val params = EditorConfig.
      mode("text/x-scala").
      lineNumbers(false).
      lineWrapping(true).
      tabSize(2).
      theme("solarized light").
      smartIndent(true).
      keyMap("sublime").
      extraKeys(js.Dictionary(
        s"$ctrl-Space" -> "autocomplete",
         "."           -> "autocompleteDot",
        s"$ctrl-Enter" -> "run",
        s"$ctrl-,"     -> "config",
        s"$ctrl-."     -> "typeAt"
      ))

    val default = 
      """|import com.scalakata._
         |@instrument class A {
         |  List(1,2,3).map(i => s"===== $i =====")mkString(System.lineSeparator)
         |}""".stripMargin

    val nl = "\n"

    dom.document.getElementById("scalakata") match {
      case el:HTMLTextAreaElement ⇒
        val editor = CodeMirror.fromTextArea(el, params)
        editor.getDoc.setValue(default)

        var insights = List.empty[LineWidget]
        // CodeMirror.commands.run = { () ⇒
          val request = EvalRequest(editor.getDoc.getValue(nl))
          Client[Api].eval(request).call().map{ response =>
            // for {
            //   (severity, infos) <- response.complilationInfos
            //   info <- infos
            // } yield {
            //   info.pos
            // }
            response.instrumentation.map{ case (RangePosition(start, _, end), repr) =>
              if(repr.contains(nl)) {
                val startPos = editor.getDoc.posFromIndex(end)
                editor.addLineWidget(startPos.line, pre(repr).render)
              } else {
                ()
              }
            }

            // dom.console.log(response.toString)

            // editor.addWidget(pos: {line, ch}, node: Element, scrollIntoView: boolean)
            // editor.addLineWidget(line: Int, node: HTMLElement, options: js.Any = js.native): LineWidget = js.native
            // editor.markText({ch: 0, line: start.line}, end, { replacedWith: e})
            ()
          }
          ()

        // }
      case _ ⇒ dom.console.error("cannot find text area for the code!")
    }
  }
}
