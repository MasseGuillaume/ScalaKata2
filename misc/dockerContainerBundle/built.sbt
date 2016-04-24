libraryDependencies ++= Seq(
  // typelevel stack
  "org.typelevel"                %% "cats"                     % "0.4.1"  ,
  "com.chuusai"                  %% "shapeless"                % "2.3.0"  ,
  "org.spire-math"               %% "spire"                    % "0.11.0" ,
  "org.spire-math"               %% "algebra"                  % "0.4.0"  ,
  "com.github.julien-truffaut"   %% "monocle-core"             % "1.2.1"  ,
  "org.scodec"                   %% "scodec-core"              % "1.9.0"  ,

  // incubators
  "com.github.stacycurl"         %% "delta-core"               % "1.0.18" ,
  "org.atnos"                    %% "eff-cats"                 % "1.4",
  "com.squants"                  %% "squants"                  % "0.5.3",
  "com.slamdata"                 %% "matryoshka-core"          % "0.9.0",

  // scalaz
  "org.scalaz"                   %% "scalaz-core"              % "7.2.2" ,
  "org.scalaz.stream"            %% "scalaz-stream"            % "0.8.1" ,


  // misc
  "com.lihaoyi"                  %% "fastparse"                % "0.3.7" ,

  // scala modules
  "org.scala-lang.modules"       %% "scala-async"              % "0.9.5" ,
  "org.scala-lang.modules"       %% "scala-parser-combinators" % "1.0.4" ,
  "org.scala-lang.modules"       %% "scala-pickling"           % "0.10.1",
  "org.scala-lang.modules"       %% "scala-xml"                % "1.0.5"
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
name         := "scalakata-bundle"
version      := "1.1.3"
description  := "Docker Container with various librairies"
