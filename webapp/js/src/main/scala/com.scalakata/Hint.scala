package com.scalakata

import autowire._
import org.denigma.codemirror._
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom
import scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scalajs.js
import scalatags.JsDom.all._
import scala.concurrent.Future

object Hint {
  def hint[T](editor: Editor, dataFun: (Int => Future[T]), renderFun: ((T, String) =>  js.Array[Hint]), single: Boolean) = {
    val doc = editor.getDoc()
    val cursor = doc.getCursor()
    val cursorIndex = doc.indexFromPos(cursor)
    
    //CodeMirror.showHin
    //              ^
    //CodeMirror.showHin
    //           ^     ^
    def findTerm = {
      val line = doc.getLine(cursor.line)
      val index = doc.getCursor().ch
      val start = line.substring(0, index)
      val end = line.substring(index, line.length)
      def notAlphaNumIndex(v: String) = {
        val alphaNum = ('a' to 'z').toSet ++ ('A' to 'Z').toSet ++ ('0' to '9').toSet ++ Set('_')
        v.zipWithIndex.find(v => !alphaNum.contains(v._1)).map(_._2)
      }
      val startIndex = notAlphaNumIndex(start.reverse).map(start.length - _).getOrElse(0)
      val endIndex = notAlphaNumIndex(end).getOrElse(line.length)
      (startIndex, endIndex, line.substring(startIndex, endIndex))
    }

    val line = doc.getLine(cursor.line)

    dataFun(cursorIndex).onSuccess{ case result =>
      CodeMirror.showHint(editor, (_, options) => {
        val (fromCh, toCh, list) =
          if(single) {  
            val (fromCh, toCh, term) = findTerm
            (fromCh, toCh, renderFun(result, term))
          } else {
            (line.length, line.length, renderFun(result, ""))
          }
        options.alignWithWord = true
        options.completeSingle = single
        js.Dictionary(
          "from" -> (Pos.ch(fromCh).line(cursor.line): Position),
          "to" -> (Pos.ch(toCh).line(cursor.line): Position),
          "list" -> list
        )
      }) 
    }
  }
  def typeAt(editor: Editor) = {
    val code = editor.getDoc().getValue()
    hint(editor, 
      pos => Client[Api].typeAt(TypeAtRequest(code, RangePosition(pos, pos, pos))).call(),
      (data: Option[TypeAtResponse], _) => data.map{ case TypeAtResponse(tpe) => 
        HintConfig.className("typeAt")
                  .text(s" // $tpe")
                  .render( (el, _, _) => {
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
      pos => Client[Api].autocomplete(CompletionRequest(code, RangePosition(pos, pos, pos))).call(),
      (data: List[CompletionResponse], term) => {
        data.filter(_.name.toLowerCase.contains(term.toLowerCase))
            .map{ case CompletionResponse(name, signature) =>
              HintConfig.className("autocomplete")
                        .text(name)
                        .render((el, _, _) => {
                          el.className = "autocomplete" 
                          el.appendChild(List(
                            span(`class` := "name")(name),
                            span(`class` := "signature")(signature)
                          ).render)
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

