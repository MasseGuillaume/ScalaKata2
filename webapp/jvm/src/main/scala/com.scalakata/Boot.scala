package com.scalakata

import scala.concurrent.duration._

import java.nio.file.Paths
import java.net.URI
import java.io.File

object Boot {
  def main(args: Array[String]): Unit = {
    val (readyPort :: artifacts :: host :: port ::
         production :: security :: timeoutS :: scalacOptions) = args.to[List]

    Server.start(
        Duration(timeoutS), security.toBoolean, production.toBoolean,
        artifacts.split(File.pathSeparatorChar).map(p => Paths.get(new URI(p))), scalacOptions, host, port.toInt, readyPort.toInt
    )
  }
}