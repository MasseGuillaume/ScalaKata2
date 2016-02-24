package com.scalakata

import scalajs.js
import scala.scalajs.js.annotation.JSName
import org.scalajs.dom.raw.HTMLElement

@JSName("Markdown")
@js.native
object Pagedown extends js.Object {
  def getSanitizingConverter(): MarkdownConverter = js.native
}

@js.native
trait MarkdownConverter extends js.Object {
  def makeHtml(value: String): String = js.native
}

@js.native
object RegexHelper extends js.GlobalScope {
  def codeReg: scalajs.js.RegExp = js.native
}