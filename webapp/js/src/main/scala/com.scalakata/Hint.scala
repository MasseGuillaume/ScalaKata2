package com.scalakata

import autowire._
import org.denigma.codemirror._
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import scalajs.js
import scalatags.JsDom.all._
import scala.concurrent.Future

object Hint {
  def hint[T](editor: Editor, dataFun: (Int ⇒ Future[T]), renderFun: ((T, String) ⇒  js.Array[Hint]), single: Boolean) = {
    val doc = editor.getDoc()
    val cursor = doc.getCursor()
    val cursorIndex = doc.indexFromPos(cursor)
    
    //CodeMirror.showHin
    //              ^
    //CodeMirror.showHin
    //           ^     ^
    def findTerm: (Int, Int, String) = {
      val line = doc.getLine(cursor.line)
      val index = doc.getCursor().ch
      val alphaNum = ('a' to 'z').toSet ++ ('A' to 'Z').toSet ++ ('0' to '9').toSet ++ Set('_')
      var to = index
      var from = index

      {
        var i = index - 1
        while(i >= 0 && alphaNum.contains(line(i))) {
          from = i
          i -= 1
        }
      }

      {
        var i = index
        while(i < line.length && alphaNum.contains(line(i))) {
          to = i + 1
          i += 1
        }
      }
      
      val term = line.substring(from, to)
      (from, to, term)
    }

    val line = doc.getLine(cursor.line)

    dataFun(cursorIndex).onSuccess{ case result ⇒
      CodeMirror.showHint(editor, (_, options) ⇒ {
        val (fromCh, toCh, list) =
          if(single) {  
            val (fromCh, toCh, term) = findTerm
            (fromCh, toCh, renderFun(result, term))
          } else {
            (line.length, line.length, renderFun(result, ""))
          }
        
        js.Dictionary(
          "from" -> (Pos.ch(fromCh).line(cursor.line): Position),
          "to" -> (Pos.ch(toCh).line(cursor.line): Position),
          "list" -> list
        )
      }, js.Dictionary(
        "container" -> dom.document.getElementById("code").querySelector(".CodeMirror"),
        "alignWithWord" -> true,
        "completeSingle" -> single
      )) 
    }
  }
  def typeAt(editor: Editor) = {
    val code = editor.getDoc().getValue()
    hint(editor, 
      pos ⇒ Client[Api].typeAt(TypeAtRequest(code, RangePosition(pos, pos, pos))).call(),
      (data: Option[TypeAtResponse], _) ⇒ data.map{ case TypeAtResponse(tpe) ⇒ 
        HintConfig.className("typeAt")
                  .text(s" // $tpe")
                  .render( (el, _, _) ⇒ {
                    el.appendChild(pre(tpe).render)
                    ()
                  }): Hint
      }.to[js.Array],
      single = false
    )
  }
  def autocomplete(editor: Editor) = {
    val code = editor.getDoc().getValue()
    hint(editor, 
      pos ⇒ Client[Api].autocomplete(CompletionRequest(code, RangePosition(pos, pos, pos))).call(),
      (data: List[CompletionResponse], term) ⇒ {
        data.filter(_.name.toLowerCase.contains(term.toLowerCase))
            .map{ case CompletionResponse(name, signature) ⇒
              HintConfig.className("autocomplete")
                        .text(name)
                        .render((el, _, _) ⇒ {
                          val node = pre(`class` := "signature").render
                          CodeMirror.runMode(signature, Rendering.modeScala, node)
                          el.appendChild(span(`class` := "name cm-def")(name).render)
                          el.appendChild(node)
                          ()
                        }): Hint
            }.to[js.Array]
      },
      single = true
    )
  }
  def autocompleteDot(editor: Editor) = {
    editor.getDoc().replaceSelection(".")
    autocomplete(editor)
  }
}

