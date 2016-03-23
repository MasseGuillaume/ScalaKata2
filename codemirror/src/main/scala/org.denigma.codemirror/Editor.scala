package org.denigma.codemirror

import org.scalajs._
import org.scalajs.dom.raw.{HTMLTextAreaElement, Event, HTMLElement, Element}
import scala.scalajs.js
import scala.scalajs.js._
import scala.scalajs.js.annotation.JSName

@js.native
trait Editor extends js.Object {
  def hasFocus(): Boolean = js.native
  def findPosH(start: Position, amount: Double, unit: String, visually: Boolean): js.Any = js.native
  def findPosV(start: Position, amount: Double, unit: String): js.Any = js.native
  def setOption(option: String, value: js.Any): Unit = js.native
  def getOption(option: String): js.Dynamic = js.native
  def addKeyMap(map: js.Any, bottom: Boolean = js.native): Unit = js.native
  def removeKeyMap(map: js.Any): Unit = js.native
  def addOverlay(mode: js.Any, options: js.Any = js.native): Unit = js.native
  def removeOverlay(mode: js.Any): Unit = js.native
  def getDoc(): Doc = js.native
  def swapDoc(doc: Doc): Doc = js.native
  def setGutterMarker(line: js.Any, gutterID: String, value: HTMLElement): LineHandle = js.native
  def clearGutter(gutterID: String): Unit = js.native
  def addLineClass(line: js.Any, where: String, _clazz: String): LineHandle = js.native
  def removeLineClass(line: js.Any, where: String, clazz: String): LineHandle = js.native
  def lineInfo(line: js.Any): js.Any = js.native
  def addWidget(pos: Position, node: HTMLElement, scrollIntoView: Boolean = js.native): Unit = js.native
  def addLineWidget(line: Int, node: HTMLElement, options: js.Any = js.native): LineWidget = js.native
  def setSize(width: js.Any, height: js.Any): Unit = js.native
  def scrollTo(x: Double, y: Double): Unit = js.native
  def getScrollInfo(): js.Any = js.native
  def scrollIntoView(pos: Position, margin: Double = js.native): Unit = js.native
  def cursorCoords(pos: Position, mode: String): Coords = js.native
  def charCoords(pos: Position, mode: String): js.Any = js.native
  def coordsChar(`object`: js.Any, mode: String = js.native): Position = js.native
  def defaultTextHeight(): Double = js.native
  def defaultCharWidth(): Double = js.native
  def getViewport(): js.Any = js.native
  def refresh(): Unit = js.native
  def indentSelection(how: String): Unit = js.native
  def getTokenAt(pos: Position): js.Any = js.native
  def getStateAfter(line: Double = js.native): js.Dynamic = js.native
  def operation[T](fn: js.Function0[T]): T = js.native
  def indentLine(line: Int, dir: js.Any = js.native): Unit = js.native
  def focus(): Unit = js.native
  def getInputField(): HTMLTextAreaElement = js.native
  def getWrapperElement(): HTMLElement = js.native
  def getScrollerElement(): HTMLElement = js.native
  def getGutterElement(): HTMLElement = js.native
  def execCommand(command: String): Unit = js.native
  def somethingSelected(): Boolean = js.native
  def on(eventName: String, handler: js.Function2[Editor, js.Any, Unit]): Unit = js.native
  def off(eventName: String, handler: js.Function2[Editor, js.Any, Unit]): Unit = js.native
}

@js.native
@JSName("Doc")
class Doc protected () extends js.Object {
  def this(text: String, mode: js.Any = js.native, firstLineNumber: Double = js.native) = this()
  def getValue(seperator: String = js.native): String = js.native
  def setValue(content: String): Unit = js.native
  def getRange(from: Position, to: Position, seperator: String = js.native): String = js.native
  def replaceRange(replacement: String, from: Position): Unit = js.native
  def replaceRange(replacement: String, from: Position, to: Position): Unit = js.native

  def getLine(n: Int): String = js.native
  def lineCount(): Int = js.native
  def firstLine(): Int = js.native
  def lastLine(): Int = js.native
  def getLineHandle(num: Int): LineHandle = js.native
  def getLineNumber(handle: LineHandle): Int = js.native
  def eachLine(f: js.Function1[LineHandle, Unit]): Unit = js.native
  def eachLine(start: Int, end: Int, f: js.Function1[LineHandle, Unit]): Unit = js.native
  def markClean(): Unit = js.native
  def isClean(): Boolean = js.native
  def getSelection(): String = js.native
  def replaceSelection(replacement: String, collapse: String = js.native): Unit = js.native
  def getCursor(start: String = js.native): Position = js.native
  def somethingSelected(): Boolean = js.native
  def setCursor(pos: Position): Unit = js.native
  def listSelections(): js.Array[Range] = js.native
  def setSelection(anchor: Position, head: Position): Unit = js.native
  def extendSelection(from: Position, to: Position = js.native): Unit = js.native
  def setExtending(value: Boolean): Unit = js.native
  def getEditor(): Editor = js.native
  def copy(copyHistory: Boolean): Doc = js.native
  def linkedDoc(options: js.Any): Doc = js.native
  def unlinkDoc(doc: Doc): Unit = js.native
  def iterLinkedDocs(fn: js.Function2[Doc, Boolean, Unit]): Unit = js.native
  def undo(): Unit = js.native
  def redo(): Unit = js.native
  def historySize(): js.Any = js.native
  def clearHistory(): Unit = js.native
  def getHistory(): js.Dynamic = js.native
  def setHistory(history: js.Any): Unit = js.native
  def markText(from: Position, to: Position, options: TextMarkerOptions = js.native): TextMarker = js.native
  def setBookmark(pos: Position, options: js.Any = js.native): TextMarker = js.native
  def findMarksAt(pos: Position): js.Array[TextMarker] = js.native
  def getAllMarks(): js.Array[TextMarker] = js.native
  def getMode(): js.Dynamic = js.native
  def posFromIndex(index: Int): Position = js.native
  def indexFromPos(position: Position): Int = js.native
}

@js.native
trait Coords extends js.Object {
  val left: Double = js.native
  val top: Double = js.native
  val bottom: Double = js.native
}

@js.native
trait Range extends js.Object {
  val anchor: Position = js.native
  val head: Position = js.native
}

@js.native
trait LineHandle extends js.Object {
  val text: String = js.native
}

@js.native
trait TextMarker extends js.Object {
  def clear(): Unit = js.native
  def find(): Position = js.native
  def getOptions(copyWidget: Boolean): TextMarkerOptions = js.native
}

@js.native
trait LineWidget extends js.Object {
  def clear(): Unit = js.native
  def changed(): Unit = js.native
}

@js.native
trait EditorBeforeChange extends js.Object {
  def cancel(): Unit = js.native
  val canceled: Boolean = js.native
  val from: Position = js.native
  val origin: String = js.native
  val text: js.Array[String] = js.native
  val to: Position = js.native
  def update(from: Position, to: Position, text: String, origin: String): Unit = js.native 
}

@js.native
trait EditorChange extends js.Object {
  val from: Position = js.native
  val to: Position = js.native
  val text: js.Array[String] = js.native
  val removed: js.Array[String] = js.native
  val origin: String = js.native
}

@js.native
trait EditorChangeLinkedList extends EditorChange {
  var next: EditorChangeLinkedList = js.native
}

@js.native
trait EditorChangeCancellable extends EditorChange {
  def update(from: Position = js.native, to: Position = js.native, text: String = js.native): Unit = js.native
  def cancel(): Unit = js.native
}

@js.native
trait Position extends js.Object {
  var ch: Int = js.native
  var line: Int = js.native
}

@js.native
trait EditorConfiguration extends js.Object {
  // string|CodeMirror.Doc
  // The starting value of the editor. Can be a string, or a document object.
  var value: js.Any = js.native
  // string|object
  // The mode to use. When not given, this will default to the first mode that was loaded. It may be a string, which either simply names the mode or is a MIME type associated with the mode. Alternatively, it may be an object containing configuration options for the mode, with a name property that names the mode (for example {name: "javascript", json: true}). The demo pages for each mode contain information about what configuration parameters the mode supports. You can ask CodeMirror which modes and MIME types have been defined by inspecting the CodeMirror.modes and CodeMirror.mimeModes objects. The first maps mode names to their constructors, and the second maps MIME types to mode specs.
  var mode: js.Any = js.native
  // The theme to style the editor with. You must make sure the CSS file defining the corresponding .cm-s-[name] styles is loaded (see the theme directory in the distribution). The default is "default", for which colors are included in codemirror.css. It is possible to use multiple theming classes at once—for example "foo bar" will assign both the cm-s-foo and the cm-s-bar classes to the editor.
  var theme: String = js.native
  // How many spaces a block (whatever that means in the edited language) should be indented. The default is 2.
  var indentUnit: Int = js.native
  // Whether to use the context-sensitive indentation that the mode provides (or just indent the same as the line before). Defaults to true.
  var smartIndent: Boolean = js.native
  // The width of a tab character. Defaults to 4.
  var tabSize: Int = js.native
  // Whether, when indenting, the first N*tabSize spaces should be replaced by N tabs. Default is false.
  var indentWithTabs: Boolean = js.native
  // Configures whether the editor should re-indent the current line when a character is typed that might change its proper indentation (only works if the mode supports indentation). Default is true.
  var electricChars: Boolean = js.native
  // A regular expression used to determine which characters should be replaced by a special placeholder. Mostly useful for non-printing special characters. The default is /[\u0000-\u0019\u00ad\u200b-\u200f\u2028\u2029\ufeff]/.
  // specialChars: RegExp
  // A function that, given a special character identified by the specialChars option, produces a DOM node that is used to represent the character. By default, a red dot (•) is shown, with a title tooltip to indicate the character code.
  // specialCharPlaceholder: function(char) → Element
  // Determines whether horizontal cursor movement through right-to-left (Arabic, Hebrew) text is visual (pressing the left arrow moves the cursor left) or logical (pressing the left arrow moves to the next lower index in the string, which is visually right in right-to-left text). The default is false on Windows, and true on other platforms.
  var rtlMoveVisually: Boolean = js.native
  // Configures the key map to use. The default is "default", which is the only key map defined in codemirror.js itself. Extra key maps are found in the [key map directory](http://codemirror.net/keymap/). See the [section on key maps](http://codemirror.net/doc/manual.html#keymaps) for more information.
  // TODO: Default | Emacs | Sublime | Vim
  var keyMap: String = js.native
  // Can be used to specify extra key bindings for the editor, alongside the ones defined by keyMap. Should be either null, or a valid key map value.
  var extraKeys: js.Any = js.native
  // Whether CodeMirror should scroll or wrap for long lines. Defaults to false (scroll).
  var lineWrapping: Boolean = js.native
  // Whether to show line numbers to the left of the editor.
  var lineNumbers: Boolean = js.native
  // At which number to start counting lines. Default is 1.
  var firstLineNumber: Int = js.native
  // A function used to format line numbers. The function is passed the line number, and should return a string that will be shown in the gutter.
  var lineNumberFormatter: js.Function1[Int, String] = js.native
  // Can be used to add extra gutters (beyond or instead of the line number gutter). Should be an array of CSS class names, each of which defines a width (and optionally a background), and which will be used to draw the background of the gutters. May include the CodeMirror-linenumbers class, in order to explicitly set the position of the line number gutter (it will default to be to the right of all other gutters). These class names are the keys passed to [setGutterMarker](http://codemirror.net/doc/manual.html#setGutterMarker).
  var gutters: js.Array[String] = js.native
  // Determines whether the gutter scrolls along with the content horizontally (false) or whether it stays fixed during horizontal scrolling (true, the default).
  var fixedGutter: Boolean = js.native
  // Chooses a scrollbar implementation. The default is "native", showing native scrollbars. The core library also provides the "null" style, which completely hides the scrollbars. Addons can implement additional scrollbar models.
  var scrollbarStyle: String = js.native

  // When fixedGutter is on, and there is a horizontal scrollbar, by default the gutter will be visible to the left of this scrollbar. If this option is set to true, it will be covered by an element with class CodeMirror-gutter-filler.
  // coverGutterNextToScrollbar: boolean

  // When fixedGutter is on, and there is a horizontal scrollbar, by default the gutter will be visible to the left of this scrollbar. If this option is set to true, it will be covered by an element with class CodeMirror-gutter-filler.
  // inputStyle: string

  // boolean|string
  // This disables editing of the editor content by the user. If the special value "nocursor" is given (instead of simply true), focusing of the editor is also disallowed.
  var readOnly: js.Any = js.native
  // Whether the cursor should be drawn when a selection is active. Defaults to false.
  var showCursorWhenSelecting: Boolean = js.native
  // When enabled, which is the default, doing copy or cut when there is no selection will copy or cut the whole lines that have cursors on them.
  // lineWiseCopyCut: boolean
  // The maximum number of undo levels that the editor stores. Note that this includes selection change events. Defaults to 200.
  var undoDepth: Double = js.native
  // The period of inactivity (in milliseconds) that will cause a new history event to be started when typing or deleting. Defaults to 1250.
  var historyEventDelay: Int = js.native
  // The tab index to assign to the editor. If not given, no tab index will be assigned.
  var tabindex: Int = js.native
  // Can be used to make CodeMirror focus itself on initialization. Defaults to off. When fromTextArea is used, and no explicit value is given for this option, it will be set to true when either the source textarea is focused, or it has an autofocus attribute and no other element is focused.
  var autofocus: Boolean = js.native

  // Controls whether drag-and-drop is enabled. On by default.
  var dragDrop: Boolean = js.native
  // var onDragEvent: js.Function2[Editor, Event, Boolean] = js.native
  // var onKeyEvent: js.Function2[Editor, Event, Boolean] = js.native
  // Half-period in milliseconds used for cursor blinking. The default blink rate is 530ms. By setting this to zero, blinking can be disabled. A negative value hides the cursor entirely.
  var cursorBlinkRate: Double = js.native
  // How much extra space to always keep above and below the cursor when approaching the top or bottom of the visible view in a scrollable document. Default is 0.
  // cursorScrollMargin: number
  // Determines the height of the cursor. Default is 1, meaning it spans the whole height of the line. For some fonts (and by some tastes) a smaller height (for example 0.85), which causes the cursor to not reach all the way to the bottom of the line, looks better
  var cursorHeight: Double = js.native
  // Controls whether, when the context menu is opened with a click outside of the current selection, the cursor is moved to the point of the click. Defaults to true.
  var resetSelectionOnContextMenu: Boolean = js.native

  // Highlighting is done by a pseudo background-thread that will work for workTime milliseconds, and then use timeout to sleep for workDelay milliseconds. The defaults are 200 and 300, you can change these options to make the highlighting more or less aggressive.
  var workTime: Double = js.native
  var workDelay: Double = js.native

  // Indicates how quickly CodeMirror should poll its input textarea for changes (when focused). Most input is captured by events, but some things, like IME input on some browsers, don't generate events that allow CodeMirror to properly detect it. Thus, it polls. Default is 100 milliseconds.
  var pollInterval: Double = js.native
  // By default, CodeMirror will combine adjacent tokens into a single span if they have the same class. This will result in a simpler DOM tree, and thus perform better. With some kinds of styling (such as rounded corners), this will change the way the document looks. You can set this option to false to disable this behavior.
  var flattenSpans: Boolean = js.native
  // When enabled (off by default), an extra CSS class will be added to each token, indicating the (inner) mode that produced it, prefixed with "cm-m-". For example, tokens from the XML mode will get the cm-m-xml class.
  // addModeClass: boolean
  // When highlighting long lines, in order to stay responsive, the editor will give up and simply style the rest of the line as plain text when it reaches a certain position. The default is 10 000. You can set this to Infinity to turn off this behavior.
  var maxHighlightLength: Double = js.native
  // Specifies the amount of lines that are rendered above and below the part of the document that's currently scrolled into view. This affects the amount of updates needed when scrolling, and the amount of work that such an update does. You should usually leave it at its default, 10. Can be set to Infinity to make sure the whole document is always rendered, and thus the browser's text search works on it. This will have bad effects on performance of big documents.
  var viewportMargin: Double = js.native

  var autoCloseBrackets: Boolean = js.native
  var matchBrackets: Boolean = js.native
  var highlightSelectionMatches: Boolean = js.native
}

@js.native
trait TextMarkerOptions extends js.Object {
  var className: String = js.native
  var inclusiveLeft: Boolean = js.native
  var inclusiveRight: Boolean = js.native
  var atomic: Boolean = js.native
  var collapsed: Boolean = js.native
  var clearOnEnter: Boolean = js.native
  var replacedWith: HTMLElement = js.native
  var readOnly: Boolean = js.native
  var addToHistory: Boolean = js.native
  var startStyle: String = js.native
  var endStyle: String = js.native
  var shared: Boolean = js.native
}

@js.native
trait LineStream extends js.Object {
  // Returns true only if the stream is at the end of the line.
  def eol: Boolean = js.native
  // Returns true only if the stream is at the start of the line.
  def sol: Boolean = js.native
  // Returns the next character in the stream without advancing it. Will return an null at the end of the line.
  def peek: String = js.native
  // Returns the next character in the stream and advances it. Also returns null when no more characters are available.
  def next: String = js.native
  // match can be a character, a regular expression, or a function that takes a character and returns a boolean. If the next character in the stream 'matches' the given argument, it is consumed and returned. Otherwise, undefined is returned.
  // eat(match: string|regexp|function(char: string) → boolean) → string
  
  // Repeatedly calls eat with the given argument, until it fails. Returns true if any characters were eaten.
  // eatWhile(match: string|regexp|function(char: string) → boolean) → boolean

  // Shortcut for eatWhile when matching white-space.
  def eatSpace: Boolean = js.native

  // Moves the position to the end of the line.
  def skipToEnd(): Unit = js.native

  // Skips to the next occurrence of the given character, if found on the current line (doesn't advance the stream if the character does not occur on the line). Returns true if the character was found.
  def skipTo(ch: String): Boolean = js.native

  // Act like a multi-character eat—if consume is true or not given—or a look-ahead that doesn't update the stream position—if it is false. pattern can be either a string or a regular expression starting with ^. When it is a string, caseFold can be set to true to make the match case-insensitive. When successfully matching a regular expression, the returned value will be the array returned by match, in case you need to extract matched groups.
  // match(pattern: string, ?consume: boolean, ?caseFold: boolean) → boolean
  // match(pattern: regexp, ?consume: boolean) → array<string>
  
  // Backs up the stream n characters. Backing it up further than the start of the current token will cause things to break, so be careful.
  def backUp(n: Integer): Unit = js.native

  // Returns the column (taking into account tabs) at which the current token starts.
  def column: Integer = js.native

  // Tells you how far the current line has been indented, in spaces. Corrects for tab characters.
  def indentation: Integer = js.native
  
  // Get the string between the start of the current token and the current stream position.
  def current: String = js.native
}

@js.native
trait ShowHintOptions extends js.Object {
  var alignWithWord: Boolean = js.native
  var async: Boolean = js.native
  // var closeCharacters: js.Regex = js.native
  var closeOnUnfocus: Boolean = js.native
  var completeOnSingleClick: Boolean = js.native
  var completeSingle: Boolean = js.native
  var container: Element = js.native
  // var customKeys: js.Dictionary = js.native
  // var extraKeys: js.Dictionary = js.native
  // var hint: js.Any = js.native
  // var hintOptions: js.Any = js.native
  // var words: js.Any = js.native
}

@js.native
@JSName("CodeMirror")
object CodeMirror extends js.Object {

  var Pass: js.Any = js.native
  def fromTextArea(host: HTMLTextAreaElement, options: EditorConfiguration = js.native): Editor = js.native
  def runMode(value: String, mode: String, dst: HTMLElement): Editor = js.native
  var version: String = js.native
  var commands: js.Dynamic = js.native
  def defineExtension(name: String, value: js.Any): Unit = js.native
  def defineDocExtension(name: String, value: js.Any): Unit = js.native
  def defineOption(name: String, default: js.Any, updateFunc: js.Function): Unit = js.native
  def defineInitHook(func: js.Function): Unit = js.native
  def defineMode(name: String, func: js.Function2[js.Object, js.Object, js.Object]): Unit = js.native

  def showHint(editor: Editor, func: js.Function2[Editor, ShowHintOptions, js.Any], options: js.Any): Unit = js.native

  // http://codemirror.net/doc/manual.html#events
  def on(element: js.Any, eventName: String, handler: js.Function): Unit = js.native
  def off(element: js.Any, eventName: String, handler: js.Function): Unit = js.native
}
