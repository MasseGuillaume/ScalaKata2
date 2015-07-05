package com.scalakata

object Template {
  import scalatags.Text.all._
  import scalatags.Text.tags2.title

  val txt =
    "<!DOCTYPE html>" +
    html(
      head(
        title("ScalaKata"),
        meta(httpEquiv:="Content-Type", content:="text/html; charset=UTF-8"),
        link(rel:="stylesheet", href:="/lib/codemirror/lib/codemirror.css"),
        link(rel:="stylesheet", href:="/main.css")
      ),
      body(
        div(`class`:="code")(
          textarea(id:="scalakata")
        ),
        script(src:="/client-fastopt.js"),
        script(src:="/lib/codemirror/lib/codemirror.js"),
        script(src:="/lib/codemirror/mode/clike/clike.js"),
        script(src:="/lib/codemirror/addon/search/searchcursor.js"),
        script(src:="/lib/codemirror/keymap/sublime.js"),
        script("com.scalakata.ScalaJSExample().main()")
      )
    )
}