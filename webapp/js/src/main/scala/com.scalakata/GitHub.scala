package com.scalakata

import scala.concurrent._
import org.scalajs.dom
import scalajs.js
import scalajs.concurrent.JSExecutionContext.Implicits.queue

object GitHub {
  def share(content: String): Future[String] = post(content).map(id(_))

  def fetch(gistId: String): Future[String] = dom.ext.Ajax.get(
    url = "https://api.github.com/gists/" + gistId,
    responseType = "json"
  ).map(e ⇒ content(e.response.asInstanceOf[js.Dictionary[js.Dictionary[String]]]("files")))

  private def id(xhr: dom.XMLHttpRequest) = dict(xhr)("id")
  private def dict(xhr: dom.XMLHttpRequest) = xhr.response.asInstanceOf[js.Dictionary[String]]
  private def content(files: js.Dictionary[String]): String = files(files.keys.head).asInstanceOf[js.Dictionary[String]]("content")

  private def post(content: String) = dom.ext.Ajax.post(
    url = "https://api.github.com/gists",
    data = js.JSON.stringify(js.Dictionary[js.Any](
        "description" -> "Scala Kata shared content",
        "public" -> true,
        "files" -> js.Dictionary[js.Any](
          "kata.scala" -> js.Dictionary[js.Any](
            "content" -> content,
            "language" -> "Scala"
          )
        )
      )
    ),
    responseType = "json"
  )
}
