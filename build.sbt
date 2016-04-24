lazy val commonSettings = Seq(
  commands += Command.command("cls") { state =>
    println("\033c") // xterm clear
    state
  },
  scalaVersion := "2.11.8",
  organization := "com.scalakata",
  version := "1.1.3",
  description := "Scala Interactive Playground",
  licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html")),
  homepage := Some(url("http://scalakata.com")),
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
    "-Xfatal-warnings",
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
  scalacOptions in (Compile, console) --= Seq(
    "-Yno-imports",
    "-Ywarn-unused-import"
  ),
  resolvers ++= Seq(
    "masseguillaume" at "http://dl.bintray.com/content/masseguillaume/maven",
    "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
  ),
  libraryDependencies += "org.specs2" %% "specs2-core" % "3.6.4" % "test",
  pomExtra := (
    <scm>
      <url>git@github.com:MasseGuillaume/ScalaKata2.git</url>
      <connection>scm:git:git@github.com:MasseGuillaume/ScalaKata2.git</connection>
    </scm>
  )
)

seq(commonSettings: _*)

val paradiseVersion = "2.1.0"

lazy val model = project
  .settings(commonSettings: _*)
  .settings(
    resolvers += "masseguillaume" at "http://dl.bintray.com/content/masseguillaume/maven",
    libraryDependencies ++= Seq(
      "com.lihaoyi"           %% "pprint"     % "0.4.0",
      "com.dallaway.richard" %%% "woot-model" % "0.1.1"
    )
  )
  .enablePlugins(ScalaJSPlugin)

lazy val annotation = project
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang"  % "scala-compiler"       % scalaVersion.value,
      "org.scala-lang"  % "scala-reflect"        % scalaVersion.value,
      compilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full)
    ),
    scalacOptions -= "-Ywarn-value-discard"
  ).dependsOn(model)

lazy val evaluation = project
  .settings(commonSettings: _*)
  .settings(
    scalacOptions -= "-Xfatal-warnings", // Thread.stop()
    buildInfoPackage := "com.scalakata.build",
    sourceGenerators in Test <+= (buildInfo in Compile),
    buildInfoKeys := Seq[BuildInfoKey](
      BuildInfoKey.map((fullClasspath in Runtime in annotation)){ case (_, v) ⇒ "annotationClasspath" -> v.map(_.data) },
      BuildInfoKey.map((fullClasspath in Runtime in model)){ case (_, v) ⇒ "modelClasspath" -> v.map(_.data) },
      (scalacOptions in Compile in annotation)
    )
  )
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(annotation)

lazy val webapp = crossProject.settings(
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "scalatags" % "0.5.2",
    "com.lihaoyi" %%% "upickle"   % "0.4.0",
    "com.lihaoyi" %%% "autowire"  % "0.2.5"
  )
).settings(commonSettings: _*)
 .jsSettings(
  name := "Client",
  libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.8.1"
).jvmSettings(Revolver.settings:_*)
 .jvmSettings(
  name := "Server",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http-experimental" % "2.4.4",
    "org.webjars.bower"  % "codemirror"             % "5.14.2",
    "org.webjars.bower"  % "open-iconic"            % "1.1.1",
    "org.webjars.bower"  % "pagedown"               % "1.1.0",
    "org.webjars.bower"  % "iframe-resizer"         % "2.8.10"
  ) 
)

def andSourceMap(aFile: java.io.File) = (
  aFile,
  file(aFile.getAbsolutePath + ".map")
)

val fullOpt = (fullOptJS in (webappJS, Compile))
val fastOpt = (fastOptJS in (webappJS, Compile))

lazy val webappJS = webapp.js.dependsOn(codemirror, model)
lazy val webappJVM = webapp.jvm
  .settings(
    JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,
    mainClass in reStart := Some("com.scalakata.BootTest"),
    reStart <<= reStart.dependsOn(WebKeys.assets in Assets),
    unmanagedResourceDirectories in Compile += (WebKeys.public in Assets).value,
    resourceGenerators in Compile += Def.task {
      val (js, map) = andSourceMap(fastOpt.value.data)
      IO.copy(Seq(
        js -> target.value / js.getName,
        map -> target.value / map.getName
      )).toSeq
    }.taskValue,
    mappings in (Compile,packageBin) := (mappings in (Compile,packageBin)).value.filterNot{ case (f, r) =>
      f.getName.endsWith("-fastopt.js") ||
      f.getName.endsWith("js.map")
    } ++ {
      val (js, map) = andSourceMap(fullOpt.value.data)
      Seq(
        js -> js.getName,
        map -> map.getName
      )
    },
    watchSources ++= ((watchSources in webappJS).value ++ (watchSources in codemirror).value)
  ).dependsOn(evaluation).enablePlugins(SbtWeb, BuildInfoPlugin)

lazy val codemirror = project
  .settings(commonSettings: _*)
  .settings(
    scalacOptions -= "-Ywarn-dead-code",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom"  % "0.8.1",
      "org.querki"   %%% "querki-jsext" % "0.5"
    )
  ).enablePlugins(ScalaJSPlugin)

lazy val sbtScalaKata = project
  .settings(commonSettings: _*)
  .settings(
    sbtPlugin := true,
    name := "sbt-scalakata",
    addSbtPlugin("io.spray"          % "sbt-revolver" % "0.8.0"),
    addSbtPlugin("se.marcuslonnberg" % "sbt-docker"   % "1.3.0"),
    bintrayRepository := "sbt-plugins",
    bintrayOrganization := None,
    scalaVersion := "2.10.6",
    scalacOptions := Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-unchecked"
    )
  ).enablePlugins(BuildInfoPlugin, BintrayPlugin)
   .settings(
    buildInfoKeys := Seq(
      "paradiseVersion" -> paradiseVersion,
      BuildInfoKey.map(version){                                case (_, v) => "scalaKataVersion" -> v },
      BuildInfoKey.map(organization){                           case (_, v) => "scalaKataOrganization" -> v },
      BuildInfoKey.map(scalacOptions in (evaluation, Compile)){ case (_, v) => "evalScalacOptions" -> v },
      BuildInfoKey.map(scalaVersion in evaluation){             case (_, v) => "evalScalaVersion" -> v },
      BuildInfoKey.map(scalaVersion in webappJVM){              case (_, v) => "backendScalaVersion" -> v },
      BuildInfoKey.map(moduleName in webappJVM){                case (_, v) => "backendProject" -> v },
      BuildInfoKey.map(moduleName in annotation){               case (_, v) => "macroProject" -> v }
    ),
    buildInfoPackage := "com.scalakata.build"
  )
