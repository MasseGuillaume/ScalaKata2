package com.scalakata

import akka.http.scaladsl._
import server.Directives._
import model._
import model.headers._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import akka.util.Timeout

import java.nio.file.Path

import upickle.default.{Reader, Writer, write => uwrite, read => uread}
object AutowireServer extends autowire.Server[String, Reader, Writer]{
  def read[Result: Reader](p: String) = uread[Result](p)
  def write[Result: Writer](r: Result) = uwrite(r)
}

class ApiImpl(compiler: Compiler) extends Api {
  def autocomplete(request: CompletionRequest) = compiler.autocomplete(request)
  def eval(request: EvalRequest) = compiler.eval(request)
  def typeAt(request: TypeAtRequest) = compiler.typeAt(request)
}

class Route(api: Api, prod: Boolean)(implicit ec: ExecutionContext){
  def route =
    path("echo") {
      post {
        formFields('code){ code ⇒
          respondWithHeader(RawHeader("X-XSS-Protection", "0")) {
            complete(html(Template.echo(code)))
          }
        }
      }
    } ~
    post {
      path("api" / Segments){ s ⇒
        entity(as[String]) { e ⇒
          complete {
            AutowireServer.route[Api](api)(
              autowire.Core.Request(s, uread[Map[String, String]](e))
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
  private def html(content: String) = HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, content))
  private val index = html(Template.txt(prod))
}