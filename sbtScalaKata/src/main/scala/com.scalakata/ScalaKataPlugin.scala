package com.scalakata

import sbt._
import Keys._
import scala.concurrent.duration._

import java.nio.file.{Path, Paths}
import java.awt.Desktop
import java.io.File

import build.BuildInfo._

import spray.revolver.Actions
import spray.revolver.RevolverPlugin.Revolver

object ScalaKataPlugin extends AutoPlugin {
  object autoImport {
    
    lazy val Kata = config("kata") extend(Runtime)
    lazy val Backend = config("backend")

    case class StartArgs(
      readyPort: Some[Int],
      serverUri: URI,
      security: Boolean,
      timeout: Duration,
      classPath: Seq[Path],
      scalacOptions: Seq[String]
    ) {
      def toArgs = Seq(
        readyPort.toString,
        serverUri.getHost,
        serverUri.getPort.toString,
        security.toString,
        timeout.toString,
        classPath.
          map(_.toAbsolutePath).
          mkString(File.pathSeparator)
      ) ++ scalacOptions
    }
    
    lazy val openBrowser = TaskKey[Unit]("open-browser", "Automatically open scalakata in the browser")
    lazy val readyPort = SettingKey[Some[Int]]("ready-port", "Port to use to wait for the server before opening the browser ")
    lazy val kataUri = SettingKey[URI]("kata-uri", "The server uri")
    lazy val startArgs = TaskKey[StartArgs]("start-args", "Arguments to pass to the main method")
    lazy val startArgs2 = TaskKey[Seq[String]]("start-args2", "Arguments to pass to the main method")
    lazy val securityManager = SettingKey[Boolean]("security-manager", "Use jvm security manager")
    lazy val timeout = SettingKey[Duration]("timeout", "maximum time to wait for evaluation response")


    lazy val scalaKataSettings: Seq[Def.Setting[_]] =
      inConfig(Backend)(
        Classpaths.ivyBaseSettings ++
        Classpaths.jvmBaseSettings ++
        Defaults.compileBase ++
        Defaults.configTasks ++
        Defaults.configSettings ++
        Revolver.settings ++
        Seq(
          scalaVersion := backendScalaVersion,
          securityManager := false,
          timeout := 20.seconds,
          mainClass in Revolver.reStart := Some("com.scalakata.backend.Boot"),
          startArgs2 in Revolver.reStart := (startArgs in Revolver.reStart).value.toArgs,
          fullClasspath in Revolver.reStart <<= fullClasspath,
          Revolver.reStart <<= InputTask(Actions.startArgsParser) { args ⇒
            (
              streams,
              Revolver.reLogTag,
              thisProjectRef,
              Revolver.reForkOptions,
              mainClass in Revolver.reStart,
              fullClasspath in Revolver.reStart,
              startArgs2 in Revolver.reStart,
              args
            ).map(Actions.restartApp)
             .dependsOn(products in Compile)
          },
          kataUri := new URI("http://localhost:7331"),
          readyPort := Some(8081),
          openBrowser := {
            readyPort.value.map{ p =>
              val socket = new java.net.ServerSocket(p)
              socket.accept()
              socket.close()
              sys.props("os.name").toLowerCase match {
                case x if x contains "mac" ⇒ s"open ${kataUri.value.toString}".!
                case _ ⇒
                  if(Desktop.isDesktopSupported) Desktop.getDesktop.browse(kataUri.value)
                  else Stream("chromium", "google-chrome", "firefox").map(b => s"b ${kataUri.value.toString}".! ).find(_ == 0)
              }
            }
            ()
          },
          libraryDependencies += scalaKataOrganization %% backendProject % scalaKataVersion
        )
      ) ++
      inConfig(Kata)(
        Classpaths.ivyBaseSettings ++
        Classpaths.jvmBaseSettings ++
        Defaults.compileBase ++
        Defaults.configTasks ++
        Defaults.configSettings ++
        Seq(
          scalaVersion := evalScalaVersion,
          unmanagedResourceDirectories += sourceDirectory.value,
          libraryDependencies ++= Seq(
            compilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full),
            scalaKataOrganization % macroProject % scalaKataVersion
          )
        )
      ) ++
      Seq(
        dependencyClasspath in Kata ++= (fullClasspath in Compile).value ++ (fullClasspath in Test).value,
        startArgs in (Backend, Revolver.reStart) := StartArgs(
          (readyPort in Backend).value,
          (kataUri in Backend).value,
          (securityManager in Backend).value,
          (timeout in Backend).value,
          (fullClasspath in Kata).value.
            map(_.data).
            map(v => Paths.get(v.getAbsoluteFile.toString)),
          (scalacOptions in Kata).value
        )
        // scalacOptions in Kata ++= evalScalacOptions,
        // (
        //   if(scalacOptions.value.isEmpty) evalScalacOptions
        //   else Seq()
        // )
      )
  }
  import autoImport._
  override def requires = sbt.plugins.JvmPlugin
  override def trigger = allRequirements

  override lazy val projectSettings = scalaKataSettings
}