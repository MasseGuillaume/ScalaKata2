package com.scalakata

import autowire._
import org.denigma.codemirror._
import org.scalajs.dom
import org.scalajs.dom.navigator
import org.scalajs.dom.raw.HTMLElement
import scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scalajs.js
import scalatags.JsDom.all._
import scala.concurrent.Future
import org.scalajs.dom.KeyboardEvent



object Rendering {
  var toclear = false

  val modeScala = "text/x-scala"
  val stateButton = dom.document.getElementById("state")
  val isMac = navigator.userAgent.contains("Mac")
  val ctrlS = if(isMac) "⌘" else "Ctrl"

  def clear(doc: Doc): Unit = {
    stateButton.setAttribute("data-glyph", "media-play")
    stateButton.setAttribute("title", s"run ($ctrlS + Enter)")
    annotations.foreach(_.clear())
    doc.setCursor(doc.getCursor())
  }

  def resetCursor(doc: Doc): Unit = {
    doc.setCursor(doc.posFromIndex(prelude.length))
  }

  def run(editor: Editor) = {
    val doc = editor.getDoc()
    stateButton.setAttribute("data-glyph", "clock")
    stateButton.setAttribute("title", "evaluating ...")

    def resetDefault(): Unit = {
      if(doc.getValue().isEmpty) {
        doc.setValue(wrap(""))
        resetCursor(doc)
      }
    }
    
    editor.on("keyup", (_, event) ⇒ {
      val ev = event.asInstanceOf[KeyboardEvent]
      val esc = 27
      if(ev.keyCode == esc) {
        toclear = false
        clear(doc)
      }
    })

    editor.on("change", (_, _) ⇒ {
      dom.localStorage.setItem(localStorageKey, doc.getValue())
      resetDefault()
    })  
    resetDefault()

    val request = EvalRequest(doc.getValue())
    Client[Api].eval(request).call().onSuccess{ case response ⇒
      clear(doc)
      toclear = true
      stateButton.setAttribute("data-glyph", "circle-x")
      stateButton.setAttribute("title", s"Clear (Esc)")

      def noop[T](v: T): Unit = ()

      def nextline2(endPos: Position, node: HTMLElement, process: (HTMLElement => Unit) = noop, options: js.Any = null): Anoted = {
        process(node)
        Line(editor.addLineWidget(endPos.line, node, options))
      }

      def nextline(endPos: Position, content: String, process: (HTMLElement => Unit) = noop, options: js.Any = null): Anoted = {
        val node = div(`class` := "line")(content).render
        nextline2(endPos, node, process, options)
      }

      def fold(startPos: Position, endPos: Position, content: String, process: (HTMLElement => Unit) = noop): Anoted = {
        val node = div(`class` := "fold")(content).render
        process(node)
        Marked(doc.markText(startPos, endPos, TextMarkerConfig.replacedWith(node)))
      }
      def inline(startPos: Position, content: String, process: (HTMLElement => Unit) = noop): Anoted = {
        // inspired by blink/devtools WebInspector.JavaScriptSourceFrame::_renderDecorations
        val basePos = Pos.line(startPos.line).ch(0)
        val offsetPos = Pos.line(startPos.line).ch(doc.getLine(startPos.line).length)

        val mode = "local"
        val base = editor.cursorCoords(basePos, mode)
        val offset = editor.cursorCoords(offsetPos, mode)

        val node = div(`class` := "inline", left := offset.left - base.left)(content).render
        process(node)

        Line(editor.addLineWidget(startPos.line, node))
      }

      val complilationInfos = {
        for {
          (severity, infos) <- response.complilationInfos
          info <- infos
        } yield {
          def severityToIcon(sev: Severity) = sev match {
            case Info => "info"
            case Warning => "warning"
            case Error => "circle-x"
          }
          val sev = severity.toString.toLowerCase

          info.pos match {
            case None => {
              val node = div(`class` := s"compiler $sev")(
                i(`class`:="oi", "data-glyph".attr := severityToIcon(severity)),
                span(info.message)
              ).render
              Line(editor.addLineWidget(doc.firstLine(), node))
            }
            case Some(RangePosition(start, _, end)) => {
              val startPos = doc.posFromIndex(start)
              val tabSize = editor.getOption("tabSize").asInstanceOf[Int]

              val line = doc.getLine(startPos.line)
              
              val tabCount = line.count(_ == '\t')
              val spaceCount = line.count(_ == ' ')

              val tabs =
                (0 until tabCount).map(_ =>
                  span(`class`:="cm-tab", role := "presentation", "cm-text".attr :="  ")(" " * tabSize)
                ).toList

              val spaces = if(spaceCount != 0) List(span(" " * spaceCount)) else Nil

              val childs = 
                tabs ::: spaces ::: List(
                  span("^"),
                  i(`class`:="oi", "data-glyph".attr := severityToIcon(severity)),
                  pre(info.message)
                )

              val node = pre(`class` := s"compiler $sev")(span(childs: _*)).render
              Line(editor.addLineWidget(startPos.line, node))
            }
          }
        }
      }.toList

      val timeout =
        if(response.timeout) {
          val node = div(`class` := "timeout")(
            i(`class`:="oi", "data-glyph".attr := "timer"),
            span("evaluation timed out")
          ).render
          List(Line(editor.addLineWidget(0, node)))
        } else Nil

      val runtimeError =
        response.runtimeError.map{ case RuntimeError(message, pos) =>
          val node = div(`class` := "runtime-error")(
            i(`class`:="oi", "data-glyph".attr := "circle-x"),
            span(message)
          ).render
          nextline2(Pos.ch(0).line(pos.map(_ - 1).getOrElse(0)), node)
        }.toList

      val instrumentations = 
        response.instrumentation.map{ case (RangePosition(start, _, end), repr) ⇒
          val startPos = doc.posFromIndex(start)
          val endPos = doc.posFromIndex(end)
          repr match {
            case EString(v) ⇒ {
              if(v.contains(nl)) {
                nextline(endPos, v, n => n.className = n.className + " cm-string")
              } else {
                val quoted = '"' + v + '"'
                inline(startPos, quoted, {
                  node => CodeMirror.runMode(quoted, modeScala, node)
                  ()
                })
              }
            }
            case Other(v) ⇒ {
              val process = (node: HTMLElement) => {CodeMirror.runMode(v, modeScala, node); () }
              if(v.contains(nl)) nextline(endPos, v, process)
              else inline(startPos, v, process)             
            }
            case Markdown(v, folded) ⇒ {

              def process(elem: HTMLElement): Unit = {
                val escaper = span().render
                val content = converter.makeHtml(v)

                dom.console.log(content)

                import scala.scalajs.js.JSStringOps._
                // js regex only replace once
                def fix(f: String => String)(v0: String) = {
                  def itt(v: String): String = {
                    val t = f(v)
                    if(t == v) t
                    else itt(t)
                  }
                  itt(v0)
                }

                

                val res = 
                  fix{ v =>
                    v.jsReplace(RegexHelper.codeReg, (b: String, c: String) => {
                      val node =
                        if(c.contains(nl)) pre(`class` := "code block").render
                        else span(`class` := "code short").render
                      escaper.innerHTML = c
                      CodeMirror.runMode(escaper.textContent, modeScala, node)
                      node.outerHTML
                    })
                  }(content)

                dom.console.log(res)

                elem.innerHTML = res  
              }
              if(!folded) nextline(endPos, v, process)
              else fold(startPos, endPos, v, process)
            }
            case Html(v, folded) ⇒ {
              val process: (HTMLElement => Unit) = _.innerHTML = v
              if(!folded) nextline(endPos, v, process)
              else fold(startPos, endPos, v, process)
            }
          }
        }

      annotations = timeout ::: runtimeError ::: instrumentations ::: complilationInfos
      editor.scrollIntoView(doc.getCursor(), dom.screen.height/2)
    }
  }

  private sealed trait Anoted { def clear(): Unit }
  private case class Line(lw: LineWidget) extends Anoted { def clear() = lw.clear() }
  private case class Marked(tm: TextMarker) extends Anoted { def clear() = tm.clear() }
  private case class Widget(node: HTMLElement) extends Anoted { def clear() = {node.parentNode.removeChild(node); ()} }
  private case object Empty extends Anoted { def clear() = () }

  private var annotations = List.empty[Anoted]        
  private val converter = Pagedown.getSanitizingConverter()
 
  private val nl = '\n'
  private val prelude = 
    """|import com.scalakata._
       |
       |@instrument class Playground {
       |  """.stripMargin
    
  def wrap(code: String): String = prelude + code + nl + "}"

  val localStorageKey = "code"
}