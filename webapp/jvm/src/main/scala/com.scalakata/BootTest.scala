package com.scalakata

object BootTest {
  def main(args: Array[String]): Unit = {
    import scala.concurrent.duration._
    import java.nio.file.Paths

    Server.start(
        timeout = 20.seconds, 
        security = false,
        production = false,
        artifacts = build.BuildInfo.runtime_fullClasspath.map(v => Paths.get(v.toURI)),
        scalacOptions = build.BuildInfo.scalacOptions.to[Seq],
        host = "localhost",
        port = 8080,
        readyPort = 0
    )
  }
}