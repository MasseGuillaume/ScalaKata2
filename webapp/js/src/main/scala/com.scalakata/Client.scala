package com.scalakata

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.Dynamic.global
import scalajs.concurrent.JSExecutionContext.Implicits.runNow

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLTextAreaElement

import scalatags.JsDom.all._
import upickle._
import autowire._

import scala.util.Random
import scala.concurrent.Future

import org.denigma.codemirror._

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

    val params = EditorConfig.
      mode("text/x-scala").
      lineNumbers(false).
      lineWrapping(true).
      tabSize(2).
      theme("solarized light").
      smartIndent(true).
      keyMap("sublime")

    dom.document.getElementById("scalakata") match {
      case el:HTMLTextAreaElement ⇒
        val editor = CodeMirror.fromTextArea(el, params)
        
      case _ ⇒ dom.console.error("cannot find text area for the code!")
    }
  }
}
