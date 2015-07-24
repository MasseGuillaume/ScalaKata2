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
}