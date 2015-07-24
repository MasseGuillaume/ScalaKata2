package org.denigma
package codemirror

import org.querki.jsext._
import org.scalajs.dom.raw.Event

import scala.scalajs.js
/**
 * Builders for easier configuration
 */
object EditorConfig extends EditorConfigurationBuilder(noOpts)
class EditorConfigurationBuilder(val dict:OptMap)
  extends JSOptionBuilder[EditorConfiguration, EditorConfigurationBuilder](new EditorConfigurationBuilder(_))
{
  def value(code:String) = jsOpt("value",code)
  def mode(modeValue:String) = jsOpt("mode",modeValue)
  def theme(themeValue:String) = jsOpt("theme",themeValue)
  def indentUnit(value:Double) = jsOpt("indentUnit",value)
  def smartIndent(value:Boolean) = jsOpt("smartIndent",value)
  def tabSize(value:Double) = jsOpt("tabSize",value)
  def indentWithTabs(value: Boolean) = jsOpt("indentWithTabs",value)
  def electricChars(value: Boolean) = jsOpt("electricChars",value)
  def rtlMoveVisually(value:Boolean) = jsOpt("rtlMoveVisually",value)
  def keyMap(value: String) = jsOpt("keyMap",value)
  def extraKeys(value: js.Any) = jsOpt("extraKeys",value)
  def lineNumbers(value:Boolean) = jsOpt("lineNumbers", value)
  def lineWrapping(value:Boolean) = jsOpt("lineWrapping", value)
  def firstLineNumber(value: Double) = jsOpt("firstLineNumber",value)
  def lineNumberFormatter(value: js.Function1[Double, String]) = jsOpt("lineNumberFormatter",value)
  def gutters(value: js.Array[String]) = jsOpt("gutters",value)
  def readOnly(value:Boolean) = jsOpt("readOnly",value)
  def fixedGutter(value: Boolean) = jsOpt("fixedGutter",value)
  def showCursorWhenSelecting(value: Boolean) = jsOpt("showCursorWhenSelecting",value)
  def undoDepth(depth:Double) = jsOpt("undoDepth",depth)
  def historyEventDelay(value: Double) = jsOpt("historyEventDelay",value)
  def tabindex(value: Double) = jsOpt("tabindex",value)
  def autofocus(value: Boolean) = jsOpt("autofocus",value)
  def dragDrop(value:Boolean) = jsOpt("dragDrop",value)
  def onDragEvent(dragEventHandler:js.Function2[Editor, Event, Boolean]) = jsOpt("onDragEvent",dragEventHandler)
  def onKeyEvent(keyEventHandler:js.Function2[Editor, Event, Boolean]) = jsOpt("onKeyEvent",keyEventHandler)
  def cursorBlinkRate(value: Double) = jsOpt("cursorBlinkRate",value)
  def cursorHeight(value: Double) = jsOpt("cursorHeight",value)
  def workTime(value: Double) = jsOpt("workTime",value)
  def workDelay(value: Double) = jsOpt("workDelay",value)
  def pollInterval(value: Double) = jsOpt("pollInterval",value)
  def flattenSpans(value: Boolean) = jsOpt("flattenSpans",value)
  def maxHighlightLength(value: Double) = jsOpt("maxHighlightLength",value)
  def viewportMargin(value: Double) = jsOpt("viewportMargin",value)
  def autoCloseBrackets(value: Boolean) = jsOpt("autoCloseBrackets",value)
  def matchBrackets(value: Boolean) = jsOpt("matchBrackets",value) 
  def highlightSelectionMatches(value: js.Any) = jsOpt("highlightSelectionMatches",value) 
}
