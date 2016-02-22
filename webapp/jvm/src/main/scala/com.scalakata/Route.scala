package com.scalakata

import akka.actor._
import spray.routing._
import spray.http._
import spray.http.Uri._
import spray.util._

import spray.client.pipelining._

import scala.concurrent.duration._
import akka.util.Timeout

import java.nio.file.Path

import scala.concurrent.{Future, Await}

import upickle.default._

class RouteActor(
  override val artifacts: Seq[Path],
  override val scalacOptions: Seq[String],
  override val security: Boolean,
  override val timeout: Duration,
  override val prod: Boolean
  ) extends Actor with Route {

  def actorRefFactory = context
  def receive = runRoute(route)
}

import upickle.default.{Reader, Writer, write => uwrite, read => uread}
object AutowireServer extends autowire.Server[String, Reader, Writer]{
  def read[Result: Reader](p: String) = uread[Result](p)
  def write[Result: Writer](r: Result) = uwrite(r)
}

trait Route extends HttpService with EvalImpl {
  import system.dispatcher

  def route =
    path("echo") {
      post {
        formFields('code){ code ⇒
          respondWithHeader(HttpHeaders.RawHeader("X-XSS-Protection", "0")) {
            complete(HttpEntity(
              ContentType(MediaTypes.`text/html`, HttpCharsets.`UTF-8`),
              HttpData(Template.echo(code))
            ))
          }
        }
      }
    } ~
    post {
      path("api" / Segments){ s ⇒
        extract(_.request.entity.asString) { e ⇒
          complete {
            AutowireServer.route[Api](this)(
              autowire.Core.Request(s, read[Map[String, String]](e))
            )
          }
        }
      }
    } ~
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
    }

  implicit val system: akka.actor.ActorRefFactory = actorRefFactory
  private implicit val executionContext = actorRefFactory.dispatcher

  private val index = HttpEntity(MediaTypes.`text/html`, Template.txt(prod))
}