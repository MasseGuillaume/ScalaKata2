package com.scalakata

import akka.actor._
import spray.routing._
import spray.http._
import spray.http.Uri._
import spray.util._
import spray.httpx.encoding.Gzip
import spray.routing.directives.CachingDirectives._

import spray.routing.directives.CacheKeyer
import spray.client.pipelining._

import scala.concurrent.duration._
import akka.util.Timeout

import java.nio.file.Path

import scala.concurrent.{Future, Await}


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

import upickle._
object AutowireServer extends autowire.Server[String, upickle.Reader, upickle.Writer]{
  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}

trait Route extends HttpService with EvalImpl {
  def route =
    get {
      path("assets" / "client-fastopt.js.map") {
        getFromResource("client-fastopt.js.map")
      }
    } ~
    get {
      path("assets" / "client-fullopt.js.map") {
        getFromResource("client-fullopt.js.map")
      }
    } ~
    path("echo") {
      post {
        formFields('code){ code ⇒
          respondWithHeader(HttpHeaders.RawHeader("X-XSS-Protection", "0")) {
            val res =
             s"""|<html>
                 |<body style="margin:0">
                 |  $code
                 |  <script src="/assets/lib/iframe-resizer/js/iframeResizer.contentWindow.min.js"></script>
                 |</body>
                 |</html>""".stripMargin
            complete(HttpEntity(
              ContentType(MediaTypes.`text/html`, HttpCharsets.`UTF-8`),
              HttpData(res)
            ))
          }
        }
      }
    } ~
    cache(simpleCache) {
      encodeResponse(Gzip) {
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

  implicit val system: akka.actor.ActorRefFactory = actorRefFactory
  private implicit val executionContext = actorRefFactory.dispatcher
  private implicit val Default: CacheKeyer = CacheKeyer {
    case RequestContext(HttpRequest(_, uri, _, entity, _), _, _) => (uri, entity)
  }
  private val simpleCache = routeCache()
  private val index = HttpEntity(MediaTypes.`text/html`, Template.txt(prod))
}