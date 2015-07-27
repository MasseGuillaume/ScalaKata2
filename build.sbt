import sbt.Keys._

lazy val commonSettings = Seq(
  scalaVersion := "2.11.7",
  version := "0.1.0-SNAPSHOT",
  licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html")),
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:experimental.macros",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xexperimental",
    // "-Xfatal-warnings",
    "-Xfuture",
    "-Xlint",
    "-Ybackend:GenBCode",
    "-Ydelambdafy:method",
    "-Yinline-warnings",
    "-Yno-adapted-args",
    "-Yrangepos",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  ),
  libraryDependencies += "org.specs2" %% "specs2-core" % "3.6.2" % "test"
)

lazy val buildInfoMacro = Seq(
  buildInfoPackage := "com.scalakata.build",
  sourceGenerators in Test <+= buildInfo,
  buildInfoKeys := Seq[BuildInfoKey](
    BuildInfoKey.map((fullClasspath in Runtime in macro)){ case (k, v) ⇒ k -> v.map(_.data) },
    (scalacOptions in Compile in macro)
  )
)

lazy val model = project
  .settings(commonSettings: _*)
  .enablePlugins(ScalaJSPlugin)

lazy val macro = project
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang"  % "scala-compiler" % scalaVersion.value,
      "org.scala-lang"  % "scala-reflect"  % scalaVersion.value,
      "org.scalamacros" % s"paradise" % "2.1.0-M5" cross CrossVersion.full,
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
    ),
    scalacOptions ~= (_ filterNot (_ == "-Ywarn-value-discard"))
  ).dependsOn(model)

lazy val eval = project
  .settings(commonSettings: _*)
  .settings(buildInfoMacro: _*)
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(macro)

import spray.revolver.AppProcess
import spray.revolver.RevolverPlugin.Revolver
lazy val webapp = crossProject.settings(
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "upickle"   % "0.2.6",
    "com.lihaoyi" %%% "autowire"  % "0.2.5",
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
    "io.spray"          %% "spray-can"      % "1.3.3",
    "io.spray"          %% "spray-routing"  % "1.3.3",
    "com.typesafe.akka" %% "akka-actor"     % "2.3.11",
    "org.webjars.bower"  % "codemirror"     % "5.4.0",
    "org.webjars.bower"  % "iframe-resizer" % "2.8.10",
    "org.webjars.bower"  % "open-iconic"    % "1.1.1",
    "org.webjars.bower"  % "pagedown"       % "1.1.0"
  )
)

lazy val webappJS = webapp.js.dependsOn(codemirror, model)
lazy val webappJVM = webapp.jvm
  .settings(
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
    watchSources ++= (watchSources in webappJS).value,
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