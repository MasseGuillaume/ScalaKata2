package org.denigma
package codemirror

import org.querki.jsext._
import org.scalajs.dom.raw.{Event, HTMLElement}

import scala.scalajs.js
/**
 * Builders for easier configuration
 */
object EditorConfig extends EditorConfigurationBuilder(noOpts)
class EditorConfigurationBuilder(val dict:OptMap)
  extends JSOptionBuilder[EditorConfiguration, EditorConfigurationBuilder](new EditorConfigurationBuilder(_))
{
  def autoCloseBrackets(v: Boolean) = jsOpt("autoCloseBrackets",v)
  def autofocus(v: Boolean) = jsOpt("autofocus",v)
  def cursorBlinkRate(v: Double) = jsOpt("cursorBlinkRate",v)
  def cursorHeight(v: Double) = jsOpt("cursorHeight",v)
  def dragDrop(v:Boolean) = jsOpt("dragDrop",v)
  def electricChars(v: Boolean) = jsOpt("electricChars",v)
  def extraKeys(v: js.Any) = jsOpt("extraKeys",v)
  def firstLineNumber(v: Double) = jsOpt("firstLineNumber",v)
  def fixedGutter(v: Boolean) = jsOpt("fixedGutter",v)
  def flattenSpans(v: Boolean) = jsOpt("flattenSpans",v)
  def gutters(v: js.Array[String]) = jsOpt("gutters",v)
  def highlightSelectionMatches(v: js.Any) = jsOpt("highlightSelectionMatches",v) 
  def historyEventDelay(v: Double) = jsOpt("historyEventDelay",v)
  def indentUnit(v:Double) = jsOpt("indentUnit",v)
  def indentWithTabs(v: Boolean) = jsOpt("indentWithTabs",v)
  def keyMap(v: String) = jsOpt("keyMap",v)
  def lineNumberFormatter(v: js.Function1[Double, String]) = jsOpt("lineNumberFormatter",v)
  def lineNumbers(v:Boolean) = jsOpt("lineNumbers", v)
  def lineWrapping(v:Boolean) = jsOpt("lineWrapping", v)
  def matchBrackets(v: Boolean) = jsOpt("matchBrackets",v) 
  def maxHighlightLength(v: Double) = jsOpt("maxHighlightLength",v)
  def mode(modev:String) = jsOpt("mode",modev)
  def onDragEvent(f: js.Function2[Editor, Event, Boolean]) = jsOpt("onDragEvent",f)
  def onKeyEvent(f: js.Function2[Editor, Event, Boolean]) = jsOpt("onKeyEvent",f)
  def pollInterval(v: Double) = jsOpt("pollInterval",v)
  def readOnly(v:Boolean) = jsOpt("readOnly",v)
  def rtlMoveVisually(v: Boolean) = jsOpt("rtlMoveVisually",v)
  def scrollPastEnd(v: Boolean) = jsOpt("scrollPastEnd",v)
  def scrollbarStyle(v: String) = jsOpt("scrollbarStyle",v)
  def showCursorWhenSelecting(v: Boolean) = jsOpt("showCursorWhenSelecting",v)
  def smartIndent(v:Boolean) = jsOpt("smartIndent",v)
  def tabindex(v: Double) = jsOpt("tabindex",v)
  def tabSize(v:Double) = jsOpt("tabSize",v)
  def theme(v:String) = jsOpt("theme",v)
  def undoDepth(depth:Double) = jsOpt("undoDepth",depth)
  def value(code:String) = jsOpt("value",code)
  def viewportMargin(v: Double) = jsOpt("viewportMargin",v)
  def workDelay(v: Double) = jsOpt("workDelay",v)
  def workTime(v: Double) = jsOpt("workTime",v)
}

object TextMarkerConfig extends TextMarkerConfig(noOpts)
class TextMarkerConfig(val dict:OptMap)
  extends JSOptionBuilder[TextMarkerOptions, TextMarkerConfig](new TextMarkerConfig(_))
{

  def className(v: String) = jsOpt("className", v)
  def inclusiveLeft(v: Boolean) = jsOpt("inclusiveLeft", v)
  def inclusiveRight(v: Boolean) = jsOpt("inclusiveRight", v)
  def atomic(v: Boolean) = jsOpt("atomic", v)
  def collapsed(v: Boolean) = jsOpt("collapsed", v)
  def clearOnEnter(v: Boolean) = jsOpt("clearOnEnter", v)
  def replacedWith(v: HTMLElement) = jsOpt("replacedWith", v)
  def readOnly(v: Boolean) = jsOpt("readOnly", v)
  def addToHistory(v: Boolean) = jsOpt("addToHistory", v)
  def startStyle(v: String) = jsOpt("startStyle", v)
  def endStyle(v: String) = jsOpt("endStyle", v)
  def shared(v: Boolean) = jsOpt("shared", v)
}


object Pos extends PositionConfig(noOpts)
class PositionConfig(val dict:OptMap)
  extends JSOptionBuilder[Position, PositionConfig](new PositionConfig(_))
{
  def ch(v: Int) = jsOpt("ch", v)
  def line(v: Int) = jsOpt("line", v)
}


@js.native
trait Hint extends js.Object {
  // The completion text. This is the only required property.
  var text: String = js.native

  // The text that should be displayed in the menu.
  var displayText: String = js.native

  // A CSS class name to apply to the completion's line in the menu.
  var className: String = js.native

  // A method used to create the DOM structure for showing the completion by appending it to its first argument.
  var render: js.Function3[HTMLElement, Hint, js.Array[js.Any], Unit] = js.native

  // A method used to actually apply the completion, instead of the default behavior.
  var hint: js.Function3[HTMLElement, Hint, js.Array[js.Any], Unit] = js.native

  // Optional from position that will be used by pick() instead of the global one passed with the full list of completions.
  var from: Position = js.native
  var to: Position = js.native
}

object HintConfig extends HintConfig(noOpts)
class HintConfig(val dict:OptMap) extends JSOptionBuilder[Hint, HintConfig](new HintConfig(_))
{
  def text(v: String) = jsOpt("text", v)
  def displayText(v: String) = jsOpt("displayText", v)
  def className(v: String) = jsOpt("className", v)
  def render(v: js.Function3[HTMLElement, Hint, js.Array[js.Any], Unit]) = jsOpt("render", v)
  def hint(v: js.Function3[HTMLElement, Hint, js.Array[js.Any], Unit]) = jsOpt("hint", v)
  def from(v: Position) = jsOpt("from", v)
  def to(v: Position) = jsOpt("to", v)
}