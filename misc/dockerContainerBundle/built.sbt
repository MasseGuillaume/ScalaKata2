lazy val dependencies = Seq(
  // typelevel stack
  // "com.chuusai"                  %% "shapeless"                % "2.2.5"  ,
  "org.typelevel"                %% "cats"                     % "0.4.1"  ,
  "org.spire-math"               %% "spire"                    % "0.7.4"  , // 0.11.0 (spark-mllib -> breeze  -> ...)
  "org.spire-math"               %% "algebra"                  % "0.3.1"  ,
  "eu.timepit"                   %% "refined"                  % "0.3.5"  ,
  "com.github.julien-truffaut"   %% "monocle-core"             % "1.1.1"  ,
  "org.http4s"                   %% "http4s-blaze-client"      % "0.12.4" ,
  "org.http4s"                   %% "http4s-blaze-server"      % "0.12.4" ,

  "org.scodec"                   %% "scodec-core"              % "1.9.0"  ,

  // scalaz
  "org.scalaz"                   %% "scalaz-core"              % "7.1.4" ,
  "org.scalaz.stream"            %% "scalaz-stream"            % "0.8"  ,

  // lightbend stack
  "com.typesafe.akka"            %% "akka-http-experimental"   % "2.0.3" ,
  "com.typesafe.slick"           %% "slick"                    % "3.1.1" ,

  // spark / data science
  "org.apache.spark"             %% "spark-core"               % "1.6.1" ,
  "org.apache.spark"             %% "spark-mllib"              % "1.6.1" ,
  "org.apache.spark"             %% "spark-sql"                % "1.6.1" ,
  "org.apache.spark"             %% "spark-streaming"          % "1.6.1" ,
  "org.scalanlp"                 %% "breeze"                   % "0.11.2",

  // misc 
  "org.parboiled"                %% "parboiled"                % "2.1.1"  ,
  "ch.qos.logback"                % "logback-classic"          % "1.1.6"  ,
  "org.slf4j"                     % "slf4j-api"                % "1.7.19" ,
  "com.h2database"                % "h2"                       % "1.4.191",
  "cc.factorie"                  %% "factorie"                 % "1.1.1" ,
  "com.squants"                  %% "squants"                  % "0.5.3" ,
  
  // lihaoyi's 100 tools :P
  "com.lihaoyi"                   % "ammonite-repl_2.11.7"     % "0.5.6" ,
  "com.lihaoyi"                  %% "ammonite-ops"             % "0.5.6" ,
  "com.lihaoyi"                  %% "fastparse"                % "0.3.7" ,
  "com.lihaoyi"                  %% "scalaparse"               % "0.3.7" ,
  "com.lihaoyi"                  %% "scalarx"                  % "0.3.1" ,
  "com.lihaoyi"                  %% "scalatags"                % "0.5.4" ,
  "com.lihaoyi"                  %% "upickle"                  % "0.3.8" ,
  "com.lihaoyi"                  %% "pprint"                   % "0.3.8" ,
  "com.lihaoyi"                  %% "sourcecode"               % "0.1.1" ,

  // scala modules
  "org.scala-lang.modules"       %% "scala-async"              % "0.9.5" ,
  "org.scala-lang.modules"       %% "scala-parser-combinators" % "1.0.4" ,
  "org.scala-lang.modules"       %% "scala-pickling"           % "0.10.1",
  "org.scala-lang.modules"       %% "scala-xml"                % "1.0.5" ,

  // testing
  "org.scalacheck"               %% "scalacheck"               % "1.12.5",
  "org.specs2"                   %% "specs2-core"              % "3.7.2-scalaz-7.1.7",
  "com.lihaoyi"                  %% "utest"                    % "0.4.3"
)


enablePlugins(ScalaKataPlugin)

organization := "masseguillaume"
name := "scalakata-bundle"
version := "1.1.0"
description := "Docker Container with various librairies"

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
libraryDependencies ++= dependencies

resolvers ++= Seq(
  "oncue" at "https://dl.bintray.com/oncue/releases",
  Resolver.sonatypeRepo("releases"),
  Resolver.typesafeIvyRepo("releases")
)

securityManager in Backend := true
