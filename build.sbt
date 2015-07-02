import sbt.Keys._
import spray.revolver.AppProcess
import spray.revolver.RevolverPlugin.Revolver

lazy val commonSettings = Seq(
  scalaVersion := "2.11.6"
)


lazy val webapp = crossProject.settings(
  version := "0.1-SNAPSHOT",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "upickle" % "0.2.6",
    "com.lihaoyi" %%% "autowire" % "0.2.5",
    "com.lihaoyi" %%% "scalatags" % "0.5.2"
  )
).settings(commonSettings: _*)
 .jsSettings(
  name := "Client",
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.8.1",
    "com.lihaoyi"  %%% "scalaparse"  % "0.2.1"
  )
).jvmSettings(Revolver.settings:_*)
 .jvmSettings(
  name := "Server",
  libraryDependencies ++= Seq(
    "io.spray"          %% "spray-can"     % "1.3.3",
    "io.spray"          %% "spray-routing" % "1.3.3",
    "com.typesafe.akka" %% "akka-actor"    % "2.3.11",
    "org.webjars"        % "codemirror"    % "5.3"
  )
)

val webappJS = webapp.js dependsOn(codemirror)
val webappJVM = webapp.jvm.settings(
  Revolver.reStart <<= Revolver.reStart.dependsOn(WebKeys.assets in Assets),
  (resources in Compile) ++= {
    val go = (fastOptJS in (webappJS, Compile)).value
    Seq(
      (artifactPath in (webappJS, Compile, fastOptJS)).value,
      file((artifactPath in (webappJS, Compile, fastOptJS)).value.getAbsolutePath + ".map")
    )
  }
).enablePlugins(SbtWeb)

lazy val codemirror = project
  .settings(commonSettings: _*)
  .settings(
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom"  % "0.8.1",
  	"org.querki"   %%% "querki-jsext" % "0.5"
  )
).enablePlugins(ScalaJSPlugin)
