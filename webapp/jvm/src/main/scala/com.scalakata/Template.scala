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
        link(rel:="stylesheet", href:="/assets/lib/codemirror/lib/codemirror.css"),
        link(rel:="stylesheet", href:="/assets/main.css")
      ),
      body(
        div(`class`:="code")(
          textarea(id:="scalakata")
        ),
        script(src:="/assets/client-fastopt.js"),
        script(src:="/assets/lib/codemirror/lib/codemirror.js"),
        script(src:="/assets/lib/codemirror/mode/clike/clike.js"),
        script(src:="/assets/lib/codemirror/addon/search/searchcursor.js"),
        script(src:="/assets/lib/codemirror/keymap/sublime.js"),
        script("com.scalakata.ScalaJSExample().main()")
      )
    )
}