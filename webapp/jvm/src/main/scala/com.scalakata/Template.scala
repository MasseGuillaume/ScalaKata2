package com.scalakata

object Template {
  import scalatags.Text.all._
  import scalatags.Text.tags2.title

  val txt =
    "<!DOCTYPE html>" +
    html(
      head(
        title("ScalaKata"),
        base(href:="/"),
        meta(charset:="utf-8"),
        meta(name:="description", content:= "Interactive Playground for the Scala Programming Language"),
        link(rel:="icon", `type`:="image/png", href:="/assets/favicon.ico"),
        link(rel:="stylesheet", href:="/assets/lib/codemirror/addon/dialog/dialog.css"),
        link(rel:="stylesheet", href:="/assets/lib/codemirror/addon/fold/foldgutter.css"),
        link(rel:="stylesheet", href:="/assets/lib/codemirror/addon/hint/show-hint.css"),
        link(rel:="stylesheet", href:="/assets/lib/codemirror/lib/codemirror.css"),
        link(rel:="stylesheet", href:="/assets/lib/codemirror/theme/mdn-like.css"),
        link(rel:="stylesheet", href:="/assets/lib/open-iconic/font/css/open-iconic.css"),
        

        link(rel:="stylesheet", href:="/assets/main.css")
      ),
      body(
        div(`class`:="code")(
          textarea(id:="scalakata")
        ),
        
        script(src:="/assets/lib/codemirror/lib/codemirror.js"),

        script(src:="/assets/lib/codemirror/addon/comment/comment.js"),
        script(src:="/assets/lib/codemirror/addon/dialog/dialog.js"),
        script(src:="/assets/lib/codemirror/addon/edit/closebrackets.js"),
        script(src:="/assets/lib/codemirror/addon/edit/matchbrackets.js"),
        script(src:="/assets/lib/codemirror/addon/fold/brace-fold.js"),
        script(src:="/assets/lib/codemirror/addon/fold/foldcode.js"),
        script(src:="/assets/lib/codemirror/addon/hint/show-hint.js"),
        script(src:="/assets/lib/codemirror/addon/runmode/runmode.js"),
        script(src:="/assets/lib/codemirror/addon/search/match-highlighter.js"),
        script(src:="/assets/lib/codemirror/addon/search/search.js"),
        script(src:="/assets/lib/codemirror/addon/search/searchcursor.js"),
        script(src:="/assets/lib/codemirror/keymap/sublime.js"),
        script(src:="/assets/lib/codemirror/mode/clike/clike.js"),

        script(src:="/assets/lib/pagedown/Markdown.Converter.js"),
        script(src:="/assets/lib/pagedown/Markdown.Sanitizer.js"),
        script(src:="/assets/lib/pagedown/Markdown.Extra.js"),

        script(src:="/assets/lib/iframe-resizer/js/iframeResizer.min.js"),
        script(src:="/assets/client-fastopt.js"),
        script("com.scalakata.Main().main()")
      )
    )
}