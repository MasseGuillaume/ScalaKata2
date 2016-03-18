package com.scalakata

object BootTest {
  def main(args: Array[String]): Unit = {
    import scala.concurrent.duration._
    import java.nio.file.Paths
    import build.BuildInfo._

    Server.start(
        timeout = 20.seconds, 
        security = false,
        artifacts = (annotationClasspath ++ modelClasspath).distinct.map(v ⇒ Paths.get(v.toURI)),
        scalacOptions = scalacOptions.to[Seq],
        host = "localhost",
        port = 7331,
        readyPort = None,
        prod = false
    )
  }
}