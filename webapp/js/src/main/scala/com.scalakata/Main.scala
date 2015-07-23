package com.scalakata

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom
import dom.navigator


import org.scalajs.dom.raw.HTMLTextAreaElement
import scalatags.JsDom.all._


import org.denigma.codemirror._

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
      extraKeys(js.Object(Map(
        s"$ctrl-Space" -> "autocomplete",
         "."           -> "autocompleteDot",
        s"$ctrl-Enter" -> "run",
        s"$ctrl-,"     -> "config",
        s"$ctrl-."     -> "typeAt"
      )))

    dom.document.getElementById("scalakata") match {
      case el:HTMLTextAreaElement ⇒
        val editor = CodeMirror.fromTextArea(el, params)

        var insights = List.empty[LineWidget]

        CodeMirror.commands.run = { () ⇒
          // def addLineWidget(line: js.Any, node: HTMLElement, options: js.Any = js.native): LineWidget = js.native
          // case class EvalResponse(
          //  complilationInfos: Map[Severity, List[CompilationInfo]],
          //  timeout: Boolean,
          //  runtimeError: Option[RuntimeError],
          //  instrumentation: Instrumentation
          //)
          // import autowire._
          // import scalajs.concurrent.JSExecutionContext.Implicits.runNow

          // val request = EvalRequest(editor.getDoc.getValue("\n"))
          // Client[Api].eval(request).call().map{ response =>
          //   for {
          //     (severity, infos) <- response.complilationInfos
          //     info <- infos
          //   } yield {
          //     info.pos
          //   }

            // editor.addWidget(pos: {line, ch}, node: Element, scrollIntoView: boolean)
            // editor.addLineWidget()
            // editor.markText({ch: 0, line: start.line}, end, { replacedWith: e})
          // }
        }
      case _ ⇒ dom.console.error("cannot find text area for the code!")
    }
  }
}
