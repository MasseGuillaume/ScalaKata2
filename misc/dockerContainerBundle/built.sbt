libraryDependencies ++= Seq(
  // typelevel stack
  "org.typelevel"                %% "cats"                     % "0.6.0"  ,
  "com.chuusai"                  %% "shapeless"                % "2.3.1"  ,
  "org.spire-math"               %% "spire"                    % "0.11.0" ,
  "org.spire-math"               %% "algebra"                  % "0.4.2"  ,
  "com.github.julien-truffaut"   %% "monocle-core"             % "1.2.1"  ,
  "org.scodec"                   %% "scodec-core"              % "1.9.0"  ,
  
  // scalaz
  "org.scalaz"                   %% "scalaz-core"              % "7.2.4"  ,
  "org.scalaz.stream"            %% "scalaz-stream"            % "0.8.2a" ,

  // misc
  "com.lihaoyi"                  %% "fastparse"     % "0.3.7",
  "ai.x"                         %% "diff"          % "1.1.0",

  // scala modules
  "org.scala-lang.modules"       %% "scala-async"              % "0.9.5" ,
  "org.scala-lang.modules"       %% "scala-parser-combinators" % "1.0.4" ,
  "org.scala-lang.modules"       %% "scala-pickling"           % "0.10.1",
  "org.scala-lang.modules"       %% "scala-xml"                % "1.0.5" ,

  // quill
  "io.getquill"                  %% "quill-jdbc"               % "0.8.0"  ,
  "com.h2database"               %  "h2"                       % "1.4.192"
)

scalaVersion := "2.11.8"

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
  "-Xfuture",
  "-Xlint",
  "-Ybackend:GenBCode",
  "-Ydelambdafy:method",
  "-Yinline-warnings",
  "-Ywarn-unused-import",
  "-Yno-adapted-args",
  "-Yrangepos",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard"
)

resolvers += "stacycurl" at "https://dl.bintray.com/stacycurl/repo/"

enablePlugins(ScalaKataPlugin)
securityManager in Backend := true

organization := "masseguillaume"
name         := "scalakata-bundle-quill-stream"
version      := "1.1.5"
description  := "Docker Container with various librairies"
