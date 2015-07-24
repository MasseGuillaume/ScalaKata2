package com.scalakata

import upickle._
import autowire._

import org.scalajs.dom
import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.runNow

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

