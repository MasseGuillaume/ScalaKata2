package com.scalakata

import scalajs.js
import scala.scalajs.js.annotation.JSName
import org.scalajs.dom.raw.HTMLElement

@JSName("Markdown")
object Pagedown extends js.Object {
  def getSanitizingConverter(): MarkdownConverter = js.native
}

trait MarkdownConverter extends js.Object {
  def makeHtml(value: String): String = js.native
}
