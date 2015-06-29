import sbt.Keys._
import spray.revolver.AppProcess
import spray.revolver.RevolverPlugin.Revolver

val webapp = crossProject.settings(
  scalaVersion := "2.11.6",
  version := "0.1-SNAPSHOT",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "upickle" % "0.2.6",
    "com.lihaoyi" %%% "autowire" % "0.2.5",
    "com.lihaoyi" %%% "scalatags" % "0.5.2"
  )
).jsSettings(
  name := "Client",
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.8.0"
  )
).jvmSettings(Revolver.settings:_*)
 .jvmSettings(
  name := "Server",
  libraryDependencies ++= Seq(
    "io.spray" %% "spray-can" % "1.3.3",
    "io.spray" %% "spray-routing" % "1.3.3",
    "com.typesafe.akka" %% "akka-actor" % "2.3.11"
  )
)

val webappJS = webapp.js
val webappJVM = webapp.jvm.settings(
  (resources in Compile) += {
    (fastOptJS in (webappJS, Compile)).value
    (artifactPath in (webappJS, Compile, fastOptJS)).value
  }
)
