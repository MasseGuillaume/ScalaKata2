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
     
    val params = EditorConfig.
      mode(Rendering.modeScala).
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
        s"$ctrl-."     -> "typeAt",
        s"$ctrl-Enter" -> "run",
        // s"$ctrl-,"     -> "config", // TODO: edit configs
         "Tab"         -> "insertSoftTab"
      )).
      autoCloseBrackets(true).
      matchBrackets(true).
      showCursorWhenSelecting(true).
      autofocus(true).
      highlightSelectionMatches(js.Dictionary(
        "showToken" -> js.Dynamic.global.RegExp("\\w")
      ))
    

    CodeMirror.commands.run = Rendering.run _
    CodeMirror.commands.typeAt = Hint.typeAt _
    CodeMirror.commands.autocomplete = Hint.autocomplete _
    CodeMirror.commands.autocompleteDot = Hint.autocompleteDot _

    dom.document.getElementById("scalakata") match {
      case el:HTMLTextAreaElement ⇒ Rendering.run(CodeMirror.fromTextArea(el, params))
      case _ ⇒ dom.console.error("cannot find text area for the code!")
    }
  }
}