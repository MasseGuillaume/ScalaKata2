package com.scalakata

import scala.concurrent.duration._

import java.nio.file.Paths
import java.io.File

object Boot {
  def main(args: Array[String]): Unit = {
    val (rp :: host :: port :: security :: timeoutS :: artifacts ::  scalacOptions) = args.to[List]
    val readyPort =
      if(rp == "0") None
      else Some(rp.toInt)

    Server.start(
      FiniteDuration(timeoutS.toLong, SECONDS),
      security.toBoolean,
      artifacts.split(File.pathSeparatorChar).map(p ⇒ Paths.get(p)),
      scalacOptions,
      host,
      port.toInt,
      readyPort,
      prod = true
    )
  }
}
