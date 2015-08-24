package com.scalakata

import akka.actor._
import spray.routing.HttpService
import spray.http._
import spray.util._

import scala.concurrent.duration._

import java.nio.file.Path

class RouteActor(
  override val artifacts: Seq[Path],
  override val scalacOptions: Seq[String],
  override val security: Boolean,
  override val timeout: Duration
  ) extends Actor with Route {

  def actorRefFactory = context
  def receive = runRoute(route)
}

import upickle._
object AutowireServer extends autowire.Server[String, upickle.Reader, upickle.Writer]{
  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}

trait Route extends HttpService with EvalImpl {
  implicit val executionContext = actorRefFactory.dispatcher

  val index = HttpEntity(MediaTypes.`text/html`, Template.txt)
  val route = {
    get {
      pathSingleSlash {
       complete(index)
      } ~
      path("assets" / Rest) { path ⇒
        getFromResource(path)
      } ~
      path(Rest) { _ ⇒
        complete(index)
      }
    } ~
    post {
      path("api" / Segments){ s ⇒
        extract(_.request.entity.asString) { e ⇒
          complete {
            AutowireServer.route[Api](this)(
              autowire.Core.Request(s, upickle.read[Map[String, String]](e))
            )
          }
        }
      }
    }
  }
}