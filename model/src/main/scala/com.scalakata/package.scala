package com

package object scalakata {
  implicit val rangePositionOrdering: Ordering[(RangePosition, Render)] =
    Ordering.by{ case (rp, r) => RangePosition.unapply(rp) }

  type Instrumentation = List[(RangePosition, Render)]
  // import scala.reflect.runtime.universe.
  def render[T](a: T)(implicit m: Manifest[T]): Render = {
    a match {
      case md: Markdown ⇒ md
      case html: Html ⇒ html
      case v ⇒ Value(pprint.tokenize(v).mkString(""), m.toString)
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

  val help = html"""|<h1>Welcome to Scala Kata !</h1>
                    |Scala Kata is an interractive playground.
                    |Evaluate expressions with $Ctrl + $Enter.
                    |Clear the output with $Esc.
                    |<pre>
                    |autocomplete    $Ctrl + $Space
                    |clear           $Esc
                    |find type       $Ctrl + $Dot
                    |run             $Ctrl + $Enter
                    |toggle theme    $F2
                    |Sublime Text    See <a target="_blank" href="$sublime">Keyboard Shortcuts</a>
                    |</pre>
                    |<a target="_blank" href="https://github.com/MasseGuillaume/ScalaKata2/blob/master/dockerContainerBundle/built.sbt#L1">A lot of dependencies are included</a> with scalakata.
                    |The source code is available at <a target="_blank" href="https://github.com/MasseGuillaume/ScalaKata2">MasseGuillaume/ScalaKata2</a>
                    |published under the MIT license
                    |""".stripMargin.fold
}