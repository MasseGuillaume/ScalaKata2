package com.scalakata

object Template {
  import scalatags.Text.all._
  import scalatags.Text.tags2.{title, noscript}

  def echo(code: String) = {
    "<!DOCTYPE html>" +
    html(
      head(
        meta(charset:="utf-8"),
        link(rel:="stylesheet", href:="/assets/lib/codemirror/addon/dialog/dialog.css"),
        link(rel:="stylesheet", href:="/assets/lib/codemirror/addon/fold/foldgutter.css"),
        link(rel:="stylesheet", href:="/assets/lib/codemirror/addon/hint/show-hint.css"),
        link(rel:="stylesheet", href:="/assets/lib/codemirror/addon/scroll/simplescrollbars.css"),
        link(rel:="stylesheet", href:="/assets/lib/codemirror/lib/codemirror.css"),
        link(rel:="stylesheet", href:="/assets/lib/codemirror/theme/mdn-like.css"),
        link(rel:="stylesheet", href:="/assets/lib/open-iconic/font/css/open-iconic.css"),
        link(rel:="stylesheet", href:="/assets/main.css")
      ),
      body(style := "margin:0")(
        raw(code),
        script(src := "/assets/lib/iframe-resizer/js/iframeResizer.contentWindow.min.js")
      )
    )
  }

  def txt(prod: Boolean) = {
    val client = if(prod) "client-opt.js" else "client-fastopt.js"

    "<!DOCTYPE html>" +
    html(
      head(
        title("Scala Kata"),
        base(href:="/"),
        meta(charset:="utf-8"),
        meta(name:="description", content:= "Interactive Playground for the Scala Programming Language"),
        link(rel:="icon", `type`:="image/png", href:="/assets/favicon.ico"),
        link(rel:="stylesheet", href:="/assets/lib/codemirror/addon/dialog/dialog.css"),
        link(rel:="stylesheet", href:="/assets/lib/codemirror/addon/fold/foldgutter.css"),
        link(rel:="stylesheet", href:="/assets/lib/codemirror/addon/hint/show-hint.css"),
        link(rel:="stylesheet", href:="/assets/lib/codemirror/addon/scroll/simplescrollbars.css"),
        link(rel:="stylesheet", href:="/assets/lib/codemirror/lib/codemirror.css"),
        link(rel:="stylesheet", href:="/assets/lib/codemirror/theme/mdn-like.css"),
        link(rel:="stylesheet", href:="/assets/lib/open-iconic/font/css/open-iconic.css"),


        link(rel:="stylesheet", href:="/assets/main.css")
      ),
      body(`class` := "cm-s-solarized cm-s-dark")(
        div(`id` := "code")(
          noscript("No Javscript, No Scala!"),
          textarea(id := "scalakata", style := "display: none;"),
          ul(`class` := "menu")(
            li(id := "state", `class` := "oi", "data-glyph".attr := "media-play"),
            li(id := "theme", "title".attr := "toggle theme (F2)", `class` := "oi", "data-glyph".attr := "sun"),
            li(id := "help", "title".attr := "help (F1)", `class` := "oi", "data-glyph".attr := "question-mark"),
            li(id := "share", "title".attr := "share (F7)", `class` := "oi", "data-glyph".attr := "share-boxed")
          ),
          div(id := "shared")
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
        script(src:="/assets/lib/codemirror/addon/scroll/scrollpastend.js"),
        script(src:="/assets/lib/codemirror/addon/scroll/simplescrollbars.js"),
        script(src:="/assets/lib/codemirror/addon/search/match-highlighter.js"),
        script(src:="/assets/lib/codemirror/addon/search/search.js"),
        script(src:="/assets/lib/codemirror/addon/search/searchcursor.js"),
        script(src:="/assets/lib/codemirror/keymap/sublime.js"),
        script(src:="/assets/lib/codemirror/mode/clike/clike.js"),

        script(src:="/assets/lib/pagedown/Markdown.Converter.js"),
        script(src:="/assets/lib/pagedown/Markdown.Sanitizer.js"),
        script(src:="/assets/lib/pagedown/Markdown.Extra.js"),

        script(src:="/assets/lib/iframe-resizer/js/iframeResizer.min.js"),

        script(src:=s"/assets/$client"),
        raw("""<script>var codeReg = /<code>([\s\S]*?)<\/code>/;</script>"""),
        script("com.scalakata.Main().main()"),
        script("""
          if(window.location.hostname !== 'localhost') {
            (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
            (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
            m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
            })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

            ga('create', 'UA-42764457-1', 'auto');
            ga('send', 'pageview');
          }
        """)
      )
    )
  }
}
