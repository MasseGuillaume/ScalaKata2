package com.scalakata

import sbt._
import Keys._
import scala.concurrent.duration._

import java.nio.file.{Path, Paths}
import java.awt.Desktop
import java.io.File

import build.BuildInfo._

import spray.revolver.Actions
import spray.revolver.RevolverPlugin
import spray.revolver.RevolverPlugin.autoImport._

import sbtdocker._
import sbtdocker.DockerKeys._

object ScalaKataPlugin extends AutoPlugin {
  object autoImport {
    
    lazy val Kata = config("kata") extend(Runtime)
    lazy val Backend = config("backend")

    case class StartArgs(
      readyPort: Option[Int],
      serverUri: URI,
      security: Boolean,
      timeout: Duration,
      classPath: Seq[Path],
      scalacOptions: Seq[String]
    ) {
      def toArgs = Seq(
        readyPort.getOrElse(0).toString,
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
    lazy val readyPort = SettingKey[Option[Int]]("ready-port", "Port to use to wait for the server before opening the browser ")
    lazy val kataUri = SettingKey[URI]("kata-uri", "The server uri")
    lazy val startArgs = TaskKey[StartArgs]("start-args", "Arguments to pass to the main method")
    lazy val startArgs2 = TaskKey[Seq[String]]("start-args2", "Arguments to pass to the main method")
    lazy val securityManager = SettingKey[Boolean]("security-manager", "Use jvm security manager")
    lazy val timeout = SettingKey[Duration]("timeout", "maximum time to wait for evaluation response")


    lazy val scalaKataSettings: Seq[Def.Setting[_]] =
      addCommandAlias("kstart", ";backend:reStart ;backend:openBrowser") ++
      addCommandAlias("kdocker", "kata:docker") ++
      addCommandAlias("kstop", "backend:reStop") ++
      addCommandAlias("krestart", ";backend:reStop ;backend:reStart") ++
      inConfig(Backend)(
        Classpaths.ivyBaseSettings ++
        Classpaths.jvmBaseSettings ++
        Defaults.compileBase ++
        Defaults.configTasks ++
        Defaults.configSettings ++
        Seq(
          scalaVersion := backendScalaVersion,
          securityManager := false,
          timeout := 20.seconds,
          mainClass in reStart := Some("com.scalakata.Boot"),
          startArgs2 in reStart := (startArgs in reStart).value.toArgs,
          fullClasspath in reStart <<= fullClasspath,
          reStart <<= InputTask(Actions.startArgsParser) { args ⇒
            (
              streams,
              reLogTag,
              thisProjectRef,
              reForkOptions,
              mainClass in reStart,
              fullClasspath in reStart,
              startArgs2 in reStart,
              args
            ).map(Actions.restartApp)
             .dependsOn(products in Compile)
          },
          kataUri := new URI("http://localhost:7331"),
          readyPort := Some(8081),
          openBrowser := {
            readyPort.value.map{ p ⇒
              val socket = new java.net.ServerSocket(p)
              socket.accept()
              socket.close()
              sys.props("os.name").toLowerCase match {
                case x if x contains "mac" ⇒ s"open ${kataUri.value.toString}".!
                case _ ⇒
                  if(Desktop.isDesktopSupported) Desktop.getDesktop.browse(kataUri.value)
                  else Stream("chromium", "google-chrome", "firefox").map(b ⇒ 
                    s"$b ${kataUri.value.toString}".! 
                  ).find(_ == 0)
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
            scalaKataOrganization %% macroProject % scalaKataVersion
          )
        )
      ) ++
      Seq(
        unmanagedResourceDirectories in Backend += (sourceDirectory in Kata).value,
        resolvers += "masseguillaume" at "http://dl.bintray.com/content/masseguillaume/maven",
        dependencyClasspath in Kata ++= (fullClasspath in Compile).value ++ (fullClasspath in Test).value,
        startArgs in (Backend, reStart) := StartArgs(
          (readyPort in Backend).value,
          (kataUri in Backend).value,
          (securityManager in Backend).value,
          (timeout in Backend).value,
          (fullClasspath in Kata).value.
            map(_.data).
            map(v ⇒ Paths.get(v.getAbsoluteFile.toString)),
          (scalacOptions in Kata).value
        ),
        scalacOptions in Kata ++= evalScalacOptions
      )

      lazy val scalaKataDockerSettings: Seq[Def.Setting[_]] = 
        inConfig(Kata)(DockerSettings.baseDockerSettings) ++
        Seq(
          imageNames in (Kata, docker) := Seq(
            ImageName(
              namespace = Some(organization.value),
              repository = name.value,
              tag = Some("v" + version.value)
            )
          ),
          dockerfile in (Kata, docker) := {
            val Some(main) = (mainClass in (Backend, reStart)).value

            val app = "/app"
            val libs = s"$app/libs"
            val katas = s"$app/katas"
            val plugins = s"$app/plugins"

            val classpath = s"$libs/*:$katas/*"

            new Dockerfile {
              from("frolvlad/alpine-oraclejdk8")

              val args = {
                val t = (startArgs in (Backend, reStart)).value
                val kataClasspath =
                  (packageBin in Compile).value +:
                  (packageBin in Kata).value +:
                  (managedClasspath in Kata).value.
                     map(_.data).
                     map(_.getAbsoluteFile)

                t.copy(
                  serverUri = new URI(t.serverUri.getScheme + "://" + "0.0.0.0" + ":" + t.serverUri.getPort),
                  readyPort = None,
                  // update compiler plugin path
                  scalacOptions = t.scalacOptions.map{ v ⇒
                    val pluginArg = "-Xplugin:"
                    if(v.startsWith(pluginArg)) {
                      val plugin = file(v.slice(pluginArg.length, v.length))
                      val target = file(plugins) / plugin.name
                      stageFile(plugin,  target)
                      pluginArg + target.getAbsolutePath
                    } else v
                  },
                  // update frontend classpath
                  classPath = kataClasspath.map { v ⇒
                    val target = file(katas) / v.name
                    stageFile(v, target)
                    Paths.get(target.toURI)
                  }
                )
              }

              // backend classpath
              (managedClasspath in Backend).value.files.foreach{ dep ⇒
                val target = file(libs) / dep.name
                stageFile(dep, target)
              }
              addRaw(libs, libs)

              // frontend classpath
              addRaw(katas, katas)
              addRaw(plugins, plugins)

              // exposes
              expose(args.serverUri.getPort)
              entryPoint((
                Seq("java", "-Xmx2G", "-Xms512M", "-cp", classpath, main) ++ args.toArgs
              ):_*)
            }
          }
        )
  }
  import autoImport._
  override def requires = sbt.plugins.JvmPlugin && DockerPlugin && RevolverPlugin
  override def trigger = allRequirements

  override lazy val projectSettings = scalaKataSettings ++ scalaKataDockerSettings
}