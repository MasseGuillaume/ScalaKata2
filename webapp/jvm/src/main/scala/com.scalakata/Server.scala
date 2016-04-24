package com.scalakata

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout

import com.typesafe.config.{ConfigFactory, Config}
import scala.concurrent.duration._
import scala.concurrent.Await
import java.nio.file.Path

object Server {
  def start(timeout: Duration, security: Boolean, artifacts: Seq[Path], 
            scalacOptions: Seq[String], host: String, port: Int, readyPort: Option[Int], prod: Boolean): Unit = {

    println((timeout, security, artifacts, scalacOptions, host, port, readyPort, prod))

    val config: Config = ConfigFactory.parseString(s"""
      akka.http.server {
        idle-timeout = ${timeout.toSeconds + 5}s
      }
    """)

    implicit val system = ActorSystem("scalakata-playground", config)
    import system.dispatcher
    implicit val materializer = ActorMaterializer()

    val api = new ApiImpl(new Compiler(artifacts, scalacOptions, security, timeout))
    val route = (new Route(api, prod)).route

    val setup = 
      Http().bindAndHandle(route, host, port).map{ _ ⇒
        // notify sbt plugin to open browser
        readyPort.map{ p ⇒
          val ready = new java.net.Socket(host, p)
          ready.sendUrgentData(0)
          ready.close()
        }
        ()
      }

    Await.result(setup, 20.seconds)
  }
}