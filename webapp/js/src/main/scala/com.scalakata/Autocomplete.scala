
package com.scalakata

import autowire._
import org.denigma.codemirror._
import org.scalajs.dom.raw.HTMLElement
import scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scalajs.js
import scalatags.JsDom.all._

object Hint {
  def hint[T, A](editor: Editor, dataFun: (Int => T), renderFun: ((T, String) => List[A]), single: Boolean) = {
    val doc = editor.getDoc()
    val cursorIndex = doc.indexFromPos(doc.getCursor())
    val result = dataFun(cursorIndex)

    CodeMirror.showHint(editor, (_, options) => {      
      val line = doc.getLine(cursorIndex)
      val from = doc.getCursor()
      val to = doc.getCursor()

      //CodeMirror.showHin
      //              ^
      //CodeMirror.showHin
      //           ^     ^
      def findTerm = {
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

      val list =
        if(single) {  
          val (fromCh, toCh, term) = findTerm
          from.ch = fromCh
          to.ch = toCh
          renderFun(result, term)
        } else {
          from.ch = line.length
          to.ch = line.length
          renderFun(result, "")
        }

      options.completeSingle = single

      js.Dictionary(
        "from" -> from,
        "to" -> to,
        "list" -> list
      )
    })
  }
  def typeAt(editor: Editor) = {
    val code = editor.getDoc().getValue()
    hint(editor, 
      pos => Client[Api].typeAt(TypeAtRequest(code, RangePosition(pos, pos, pos))).call().onSuccess _,
      (data: Option[TypeAtResponse], _) => data.toList.map{ case TypeAtResponse(tpe) => js.Dictionary(
        "className" -> "typeAt",
        "text" -> s" // $tpe",
        "render" -> ((el: HTMLElement) => {
          el.appendChild(pre(tpe).render)
        })
      )},
      single = false
    )
  }
  def autocomplete(editor: Editor) = {
    val code = editor.getDoc().getValue()
    hint(editor, 
      pos => Client[Api].autocomplete(CompletionRequest(code, RangePosition(pos, pos, pos))).call().onSuccess _,
      (data: List[CompletionResponse], term) => data.
        filter(_.name.toLowerCase.contains(term.toLowerCase)).
        map{ case CompletionResponse(name, signature) => js.Dictionary(
          "className" -> "autocomplete",
          "text" -> name,
          "completion" ->  js.Dictionary(
            "name" -> name,
            "signature" -> signature
          ),
          "alignWithWord" ->  true,
          "render" -> ((el: HTMLElement) => {
            el.`class` = "autocomplete" 
            el.innerHTML = List(
              span(`class` := "name")(name),
              span(`class` := "signature")(signature)
            ).render.toString
          })
        )},
      single = true
    )
  }
  def autocompleteDot(editor: Editor) = {
    editor.getDoc().replaceSelection(".")
    autocomplete(editor)
  }
}