package com.scalakata

import woot._

import Util._

import scala.scalajs._
import org.scalajs.dom.{Position ⇒ _, _}
import org.denigma.codemirror._

import upickle.default.{Reader, Writer, write ⇒ uwrite, read ⇒ uread}

object Collaborative {
  def apply(editor: Editor): Unit = {
    var doc: Option[woot.WString] = None
    var onAir = true
    def offAir[T](body: ⇒ T): Unit = {
      onAir = false
      body
      onAir = true
    }

    def applyOp(ch: String, adding: Boolean, visiblePos: Int): Unit = {
      val cmDoc = editor.getDoc()
      val from = cmDoc.posFromIndex(visiblePos)
      val to = cmDoc.posFromIndex(visiblePos + 1)
      offAir {
        if(adding) cmDoc.replaceRange(ch, from)
        else cmDoc.replaceRange("", from, to)
      }
    }

    def applyOps(operations: List[Operation]): Unit = {
      doc.foreach( d ⇒
        operations.foldLeft(d){ case (cd, operation) ⇒
          if (operation.from != d.site) {
            val (ops, cd2) = cd.integrate(operation)
            doc = Some(cd2)
            ops.foreach{
              case InsertOp(ch, _) ⇒ applyOp(ch.alpha.toString, true,  cd2.visibleIndexOf(ch.id))
              case DeleteOp(ch, _) ⇒ applyOp(ch.alpha.toString, false, cd2.visibleIndexOf(ch.id))
            }
            cd2
          } else cd
        }
      )
      editor.refresh()
    }

    val protocol =
      if(location.protocol == "https:") "wss"
      else "ws"
    val username = java.util.UUID.randomUUID().toString()//prompt("Please enter your username", "")
    val room = location.pathname.drop("/room/".length)
    val uri = s"$protocol://${location.host}/collaborative/$room?username=$username"
    val socket = new WebSocket(uri)
    
    socket.onmessage = { e: raw.MessageEvent ⇒
      uread[CollaborationEvent](e.data.toString) match {
        case HeartBeat ⇒ ()
        case JoinedDoc(user) ⇒ console.log(s"joined $user")
        case LeftDoc(user) ⇒ console.log(s"left $user")
        case ChangeDoc(operation) ⇒ applyOps(List(operation))
        case ChangeBatchDoc(operations) ⇒ applyOps(operations)
        case SetDoc(d) ⇒ {
          doc = Some(d)
          offAir {
            editor.getDoc().setValue(d.text)
          }
        }
      }
    }

    socket.onopen = { _ ⇒
      editor.on("change", (_, e) ⇒ {
        change(e.asInstanceOf[EditorChange])
      })

      setInterval(() ⇒ socket.send(uwrite(HeartBeat)), 10000)
      
      // editor.on("cursorActivity", (_, e) ⇒ {
      //   console.log("cursorActivity", e)
      // })
      // editor.on("beforeSelectionChange", (_, e) ⇒ {
      //   console.log("beforeSelectionChange", e)
      // })
      ()
    }

    def doChange(added: Seq[String], removed: Seq[String], from: Position): Unit = {
      if(onAir) { doc.foreach{ d ⇒
        val pos = editor.getDoc().indexFromPos(from)
                
        def indexed(changes: Seq[String]): Seq[(Char, Int)] = 
          changes.mkString(nl.toString).zipWithIndex.map{ case (c, i) ⇒ (c, pos + i)}


        def foldChanges(changes: Seq[(Char, Int)], doc: WString)(f: (WString, Char, Int) ⇒ (Operation, WString)) = {
          val (os, d) = changes.foldLeft((List.empty[Operation], doc)){ case ((ops, cd), (char, i)) ⇒
            val (op, nd) = f(cd, char, i)
            (op :: ops, nd)
          }
          (os.reverse, d)
        }

        val (removeOps, d2) = foldChanges(indexed(removed).reverse, d)((cd, _, i) ⇒ cd.delete(i))
        val (addOps   , d3) = foldChanges(indexed(added), d2)((cd, char, i) ⇒ cd.insert(char, i))

        doc = Some(d3)
        if (!(addOps.isEmpty && removeOps.isEmpty)) {
          socket.send(uwrite(ChangeBatchDoc(removeOps ++ addOps)))
        }

      } }
    }

    def change(editorChange: EditorChange): Unit = {
      import editorChange._
      doChange(text, removed, from) 
    }
  }
}