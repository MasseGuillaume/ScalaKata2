package com.scalakata

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

import com.typesafe.config.{ ConfigValueFactory, ConfigFactory, Config }

import akka.util.Timeout
import scala.concurrent.duration._
import java.nio.file.Path

object Server {
  def start(timeout: Duration, security: Boolean, artifacts: Seq[Path], 
            scalacOptions: Seq[String], host: String, port: Int, readyPort: Option[Int], prod: Boolean): Unit = {

    println((timeout, security, artifacts, scalacOptions, host, port, readyPort, prod))

    val config: Config = ConfigFactory.parseString(s"""
      spray {
        can.server {
          idle-timeout = ${timeout.toSeconds + 5}s
          request-timeout = ${timeout.toSeconds + 2}s
        }
      }
    """)

    implicit val system = ActorSystem("scalakata-playground", config)

    val service = system.actorOf(Props(classOf[RouteActor],
      artifacts, scalacOptions, security, timeout, prod
    ), "scalakata-service")

    import akka.pattern.ask
    import system.dispatcher
    implicit val bindingTimeout = Timeout(5.seconds)
    (IO(Http) ? Http.Bind(service, host, port)) onSuccess {
      case _: Http.Bound ⇒ {
        readyPort.map{ p ⇒
          val ready = new java.net.Socket(host, p)
          ready.sendUrgentData(0)
          ready.close()
        }
      }
    }
    ()
  }
}
