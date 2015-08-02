package com

package object scalakata {
  implicit val rangePositionOrdering: Ordering[(RangePosition, Render)] =
    Ordering.by{ case (rp, r) => RangePosition.unapply(rp) }

  type Instrumentation = List[(RangePosition, Render)]
  
  def render[A](a: A): Render = {
    a match {
      case null ⇒ Other("null")
      case ar: Array[_] ⇒ Other(ar.deep.toString)
      case v: String ⇒  EString(v)
      case md: Markdown ⇒ md
      case html: Html ⇒ html
      case other ⇒ Other(other.toString)
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

  private val Dot = "<kbd>&nbsp;&nbsp;.&nbsp;&nbsp;</kbd>"
  private val Space = s"""<kbd>${"&nbsp;" * 40}</kbd>"""
  private val Ctrl = "<kbd>Ctrl&nbsp;&nbsp;</kbd>"
  private val Enter = "<kbd>&nbsp;&nbsp;Enter&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</kbd>"
  private val F2 = "<kbd>&nbsp;F2&nbsp;</kbd>"
  private val Esc = "<kbd>&nbsp;Esc&nbsp;</kbd>"
  private val sublime = "http://sublime-text-unofficial-documentation.readthedocs.org/en/latest/reference/keyboard_shortcuts_osx.html"

  val help = md"""|# Welcome to Scala Kata !
                  |Scala Kata is an interractive playground.
                  |Evaluate expressions with $Ctrl + $Enter.
                  |Clear the output with $Esc.
                  |## Keyboard Shorcuts
                  |<pre>
                  |autocomplete    $Ctrl + $Space
                  |clear           $Esc
                  |find type       $Ctrl + $Dot
                  |run             $Ctrl + $Enter
                  |toogle theme    $F2
                  |Sublime Text    See <a href="$sublime">Keyboard Shortcuts</a>
                  |</pre>
                  |The source code is available at [MasseGuillaume/ScalaKata2](https://github.com/MasseGuillaume/ScalaKata2)
                  |published under the MIT license
                  |""".stripMargin.fold
}