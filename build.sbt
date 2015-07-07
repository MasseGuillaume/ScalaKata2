import sbt.Keys._

lazy val commonSettings = Seq(
  scalaVersion := "2.11.7",
  licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html")),
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yinline-warnings",
    "-Yno-adapted-args",
    "-Yrangepos",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture"
  )
)

lazy val model = project
  .settings(commonSettings: _*)
  .enablePlugins(ScalaJSPlugin)

lazy val macro = project
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.specs2" %% "specs2-core" % "3.6.2" % "test"
    ),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full),
    resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
    scalacOptions ~= (_ filterNot (_ == "-Ywarn-value-discard"))
  ).dependsOn(model)

lazy val eval = project
  .settings(commonSettings: _*)
  .settings(
    
  ).dependsOn(macro)

import spray.revolver.AppProcess
import spray.revolver.RevolverPlugin.Revolver
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
    "org.webjars.bower"  % "codemirror"    % "5.4.0"
  )
)

lazy val webappJS = webapp.js.dependsOn(codemirror, model)
lazy val webappJVM = webapp.jvm.settings(
  JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,
  Revolver.reStart <<= Revolver.reStart.dependsOn(WebKeys.assets in Assets),
  (fullClasspath in Runtime) += (WebKeys.public in Assets).value,
  (resources in Compile) ++= {
    def andSourceMap(aFile: java.io.File) = Seq(
      aFile,
      file(aFile.getAbsolutePath + ".map")
    )
    andSourceMap((fastOptJS in (webappJS, Compile)).value.data)
  },
  includeFilter in (Assets, LessKeys.less) := "*.less"
).dependsOn(eval).enablePlugins(SbtWeb)

lazy val codemirror = project
  .settings(commonSettings: _*)
  .settings(
  scalacOptions ~= (_ filterNot (_ == "-Ywarn-dead-code")),
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom"  % "0.8.1",
  	"org.querki"   %%% "querki-jsext" % "0.5"
  )
).enablePlugins(ScalaJSPlugin)