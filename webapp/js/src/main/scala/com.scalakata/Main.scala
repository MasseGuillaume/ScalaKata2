package com.scalakata

import autowire._
import org.denigma.codemirror._
import org.scalajs.dom
import org.scalajs.dom.navigator
import org.scalajs.dom.raw.{HTMLTextAreaElement, HTMLElement, Node}
import org.scalajs.dom.ext.Ajax
import scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scalajs.js
import scalajs.js.annotation.JSExport
import scalatags.JsDom.all._

@JSExport
object Main {
  @JSExport
  def main(): Unit = {
    val isMac = navigator.userAgent.contains("Mac")
    val ctrl = if(isMac) "Cmd" else "Ctrl"
    val ctrlS = if(isMac) "⌘" else "Ctrl"

    val params = EditorConfig.
      mode(Rendering.modeScala).
      autofocus(true).
      lineNumbers(false).
      lineWrapping(false).
      tabSize(2).
      indentWithTabs(false).
      theme("solarized dark").
      smartIndent(true).
      keyMap("sublime").
      scrollPastEnd(true).
      scrollbarStyle("simple").
      extraKeys(js.Dictionary(
        s"$ctrl-Space" -> "autocomplete",
         "."           -> "autocompleteDot",
        s"$ctrl-."     -> "typeAt",
        s"$ctrl-Enter" -> "run",
        // s"$ctrl-,"     -> "config", // TODO: edit configs
        "F1"           -> "help",
        "F2"           -> "solarizedToggle"
      )).
      autoCloseBrackets(true).
      matchBrackets(true).
      showCursorWhenSelecting(true).
      autofocus(true).
      highlightSelectionMatches(js.Dictionary(
        "showToken" -> js.Dynamic.global.RegExp("\\w")
      ))
    
    val themeButton = dom.document.getElementById("theme")
    val stateButton = dom.document.getElementById("state")

    CodeMirror.commands.run = Rendering.run _
    CodeMirror.commands.typeAt = Hint.typeAt _
    CodeMirror.commands.autocomplete = Hint.autocomplete _
    CodeMirror.commands.autocompleteDot = Hint.autocompleteDot _
    CodeMirror.commands.help = (editor: Editor) => {
      editor.getDoc().setValue(Rendering.wrap("help"))
      Rendering.run(editor)
    }
    CodeMirror.commands.solarizedToggle = (editor: Editor) => {
      val isDark = editor.getOption("theme").asInstanceOf[String] == "solarized dark"
      val theme =
        if(isDark) "solarized light"
        else "solarized dark"

      val icon =
        if(isDark) "moon"
        else "sun"
      themeButton.setAttribute("data-glyph", icon)
      editor.setOption("theme", theme)
    }

    dom.document.getElementById("scalakata") match {
      case el:HTMLTextAreaElement ⇒ {
        val editor = CodeMirror.fromTextArea(el, params)
        val doc = editor.getDoc()
        editor.focus()
        Rendering.resetCursor(doc)
        themeButton.addEventListener("click", (e: dom.Event) => CodeMirror.commands.solarizedToggle(editor))
        dom.document.getElementById("help").addEventListener("click", (e: dom.Event) => CodeMirror.commands.help(editor))
        stateButton.setAttribute("title", s"run ($ctrlS + Enter)")
        stateButton.addEventListener("click", (e: dom.Event) => {
          if(Rendering.toclear) {
            Rendering.clear(doc)
            Rendering.toclear = false
          } else {
            Rendering.run(editor)
          }
        })

        val path = dom.location.pathname
        if(path != "/") {
          Ajax.get(s"/assets/$path").onSuccess{ case xhr =>
            doc.setValue(xhr.responseText)
            Rendering.run(editor)
          }
        } else {
          val storage = dom.localStorage.getItem(Rendering.localStorageKey)
          if(storage != null) {
            doc.setValue(storage)
            Rendering.run(editor)
          }
          else {
            CodeMirror.commands.help(editor)
            ()
          }
        }
      }
      case _ ⇒ dom.console.error("cannot find text area for the code!")
    }
  }
}