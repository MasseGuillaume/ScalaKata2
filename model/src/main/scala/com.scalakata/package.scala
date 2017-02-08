package com

package object scalakata {
  implicit val rangePositionOrdering: Ordering[(RangePosition, Render)] =
    Ordering.by{ case (rp, r) ⇒ RangePosition.unapply(rp) }

  type Instrumentation = List[(RangePosition, Render)]

  def render[T: pprint.PPrint](a: T)(implicit tp: pprint.TPrint[T]): Render = {
    a match {
      case md: Markdown ⇒ md
      case html: Html ⇒ html
      case html2: Html2 ⇒ html2
      case v ⇒ Value(pprint.tokenize(v).mkString, tp.render(pprint.Config.Defaults.PPrintConfig))
    }
  }

  implicit class MarkdownHelper(val sc: StringContext) extends AnyVal {
    def md(args: Any*) = Markdown(sc.s(args: _*))
    def mdR(args: Any*) = Markdown(sc.raw(args: _*))
  }

  implicit class HtmlHelper(val sc: StringContext) extends AnyVal {
    def html(args: Any*) = Html(sc.s(args: _*))
    def htmlR(args: Any*) = Html(sc.raw(args: _*))
  }

  implicit class Html2Helper(val sc: StringContext) extends AnyVal {
    def html2(args: Any*) = Html2(sc.s(args: _*))
    def html2R(args: Any*) = Html2(sc.raw(args: _*))
  }

  private val Dot = "<kbd>&nbsp;&nbsp;.&nbsp;&nbsp;</kbd>"
  private val Space = s"""<kbd>${"&nbsp;" * 40}</kbd>"""
  private val Ctrl = "<kbd class='pc'>Ctrl&nbsp;&nbsp;</kbd>"
  private val Cmd = "<kbd class='mac'>&nbsp;⌘&nbsp;</kbd>"
  private val Enter = "<kbd>&nbsp;&nbsp;Enter&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</kbd>"
  private val F2 = "<kbd>&nbsp;F2&nbsp;</kbd>"
  private val F7 = "<kbd>&nbsp;F7&nbsp;</kbd>"
  private val Esc = "<kbd>&nbsp;Esc&nbsp;</kbd>"
  private val sublime = "http://sublime-text-unofficial-documentation.readthedocs.org/en/latest/reference/keyboard_shortcuts_osx.html"

  val help = html"""|<h1>Welcome to Scala Kata !</h1>
                    |Scala Kata is an interactive playground.
                    |Evaluate expressions with $Ctrl $Cmd + $Enter.
                    |Clear the output with $Esc.
                    |<pre>
                    |autocomplete    $Ctrl $Cmd + $Space
                    |clear           $Esc
                    |find type       $Ctrl $Cmd + $Dot
                    |run             $Ctrl $Cmd + $Enter
                    |toggle theme    $F2
                    |share           $F7
                    |<a target="_blank" href="$sublime">Sublime Text Keyboard Shortcuts</a>
                    |</pre>
                    |<a target="_blank" href="https://github.com/MasseGuillaume/ScalaKata2/blob/master/misc/dockerContainerBundle/built.sbt#L1">A lot of dependencies are included</a> with scalakata.
                    |The source code is available at <a target="_blank" href="https://github.com/MasseGuillaume/ScalaKata2">MasseGuillaume/ScalaKata2</a>
                    |published under the MIT license
                    |Scalac ${util.Properties.versionString}
                    |""".stripMargin.fold
}
