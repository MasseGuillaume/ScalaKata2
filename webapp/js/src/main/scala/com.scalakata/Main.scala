package com.scalakata

import autowire._
import org.denigma.codemirror._
import org.scalajs.dom
import org.scalajs.dom.navigator
import org.scalajs.dom.raw.{HTMLTextAreaElement, HTMLElement, Node}
import org.scalajs.dom.ext.Ajax
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import scalajs.js
import scalajs.js.annotation.JSExport
import scalatags.JsDom.all._

@JSExport
object Main {
  @JSExport
  def main(): Unit = {
    val isMac = navigator.userAgent.contains("Mac")

    dom.document.body.className =
      if (isMac) "mac"
      else "pc"

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
        "F2"           -> "solarizedToggle",
        "F7"           -> "share"
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
    val shareButton = dom.document.getElementById("share")
    val sharedDiv = dom.document.getElementById("shared")

    CodeMirror.commands.run = Rendering.run _
    CodeMirror.commands.typeAt = Hint.typeAt _
    CodeMirror.commands.autocomplete = Hint.autocomplete _
    CodeMirror.commands.autocompleteDot = Hint.autocompleteDot _
    CodeMirror.commands.help = (editor: Editor) ⇒ {
      editor.getDoc().setValue(Rendering.wrap("help"))
      Rendering.run(editor)
    }
    CodeMirror.commands.solarizedToggle = (editor: Editor) ⇒ {
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
    CodeMirror.commands.share = (editor: Editor) ⇒ {
      GitHub.share(editor.getDoc().getValue())
        .map(id ⇒ {
          val scalaKataLink = s"${dom.window.location}?gist=$id"
          val gitHubLink = s"https://gist.github.com/anonymous/$id"
          s"Shared as: <a href='$scalaKataLink' target='_blank'>$scalaKataLink</a> (or view on <a href='$gitHubLink'>GitHub</a>)"
        })
        .recover { case t ⇒ s"Failed to share your code: ${t.getMessage}" }
        .foreach { text ⇒
          sharedDiv.setAttribute("style", "display: block")
          sharedDiv.innerHTML = text
        }
    }

    dom.document.getElementById("scalakata") match {
      case el:HTMLTextAreaElement ⇒ {
        val editor = CodeMirror.fromTextArea(el, params)
        val doc = editor.getDoc()
        editor.focus()
        Rendering.resetCursor(doc)
        themeButton.addEventListener("click", (e: dom.Event) ⇒ CodeMirror.commands.solarizedToggle(editor))
        shareButton.addEventListener("click", (e: dom.Event) ⇒ CodeMirror.commands.share(editor))
        dom.document.getElementById("help").addEventListener("click", (e: dom.Event) ⇒ CodeMirror.commands.help(editor))
        stateButton.setAttribute("title", s"run ($ctrlS + Enter)")
        stateButton.addEventListener("click", (e: dom.Event) ⇒ {
          if(Rendering.toclear) {
            Rendering.clear(doc)
            Rendering.toclear = false
          } else {
            Rendering.run(editor)
          }
        })

        val getParameters = dom.window.location.search
          .substring(1)
          .split("&")
          .map(_.split("="))
          .collect { case Array(key, value) => (key -> value) }
          .toMap

        val path = dom.location.pathname
        if(path != "/") {
          Ajax.get(s"/assets/$path").onSuccess{ case xhr ⇒
            doc.setValue(xhr.responseText)
            Rendering.run(editor)
          }
        } else if (getParameters.contains("gist")) {
          GitHub.fetch(getParameters("gist")).onSuccess{ case content ⇒
            doc.setValue(content)
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
