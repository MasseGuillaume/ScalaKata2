package com.scalakata

import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import scala.util.Random
import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scalatags.JsDom.all._
import upickle._
import autowire._

import org.denigma.codemirror.{CodeMirror, EditorConfiguration}
import org.denigma.codemirror.extensions.EditorConfig
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLTextAreaElement

object Client extends autowire.Client[String, upickle.Reader, upickle.Writer]{
  override def doCall(req: Request): Future[String] = {
    dom.ext.Ajax.post(
      url = "/api/" + req.path.mkString("/"),
      data = upickle.write(req.args)
    ).map(_.responseText)
  }

  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}

@JSExport
object ScalaJSExample {
  @JSExport
  def main(): Unit = {
    val params = EditorConfig.mode("clike").lineNumbers(true)
    val editor = dom.document.getElementById("scalakata") match {
      case el:HTMLTextAreaElement ⇒
        val m = CodeMirror.fromTextArea(el,params)
        m.getDoc().setValue("""println("hello Scala!")""")
      case _ ⇒ dom.console.error("cannot find text area for the code!")
    }

    Client[Api].hi("bob!").call().foreach { res ⇒
      dom.document.body.appendChild(
        div(res).render
      )
    }
  }
}
