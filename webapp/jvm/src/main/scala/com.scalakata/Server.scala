package com.scalakata

import upickle._
import spray.routing.SimpleRoutingApp
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext.Implicits.global
import spray.http.{MediaTypes, HttpEntity}

object Template{
  import scalatags.Text.all._
  import scalatags.Text.tags2.title
  val txt =
    "<!DOCTYPE html>" +
    html(
      head(
        title("Hi"),
        meta(httpEquiv:="Content-Type", content:="text/html; charset=UTF-8"),
        link(rel:="stylesheet", href:="/codemirror/lib/codemirror.css")
      ),
      body(
        textarea(id:="scalakata"),
        script(src:="/client-fastopt.js"),
        script(src:="/codemirror/lib/codemirror.js"),
        script(src:="/codemirror/mode/clike/clike.js"),
        script(src:="/codemirror/addon/search/searchcursor.js"),
        script(src:="/codemirror/keymap/sublime.js"),
        script("com.scalakata.ScalaJSExample().main()")
      )
    )
}
object AutowireServer extends autowire.Server[String, upickle.Reader, upickle.Writer]{
  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}
object Server extends SimpleRoutingApp with Api{
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    startServer("0.0.0.0", port = 8080) {
      get{
        pathSingleSlash {
          complete{
            HttpEntity(
              MediaTypes.`text/html`,
              Template.txt
            )
          }
        } ~
        path("codemirror" / Rest) { path ⇒
    			getFromResource(s"META-INF/resources/webjars/codemirror/5.3/$path")
    		} ~
        getFromResourceDirectory("")
      } ~
      post {
        path("api" / Segments){ s ⇒
          extract(_.request.entity.asString) { e ⇒
            complete {
              AutowireServer.route[Api](Server)(
                autowire.Core.Request(s, upickle.read[Map[String, String]](e))
              )
            }
          }
        }
      }
    }
  }

  def hi(name: String): String = {
    name.reverse
  }
}
