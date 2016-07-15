package com.scalakata

import scala.concurrent.duration._

import java.nio.file.Paths
import java.io.File

object Boot {
  def main(args: Array[String]): Unit = {
    val (rp :: host :: port :: security :: timeout :: artifacts ::  scalacOptions) = args.to[List]
    val readyPort =
      if(rp == "0") None
      else Some(rp.toInt)

    Duration(timeout) match {
      case finiteTimeout: FiniteDuration =>
        Server.start(
          finiteTimeout,
          security.toBoolean,
          artifacts.split(File.pathSeparatorChar).map(p ⇒ Paths.get(p)),
          scalacOptions,
          host,
          port.toInt,
          readyPort,
          prod = true
        )
      case _ => sys.error("timeout is not a finite duration: " + timeout)
    }

  }
}
