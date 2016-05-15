package com.scalakata

import java.io.File
import java.nio.file.Path
import scala.tools.nsc.Settings

package object evaluation {

  def toSettings(
    artifacts: Seq[Path], scalacOptions: Seq[String],
    withoutParadisePlugin: Boolean = false): Settings = {

    val settings = new Settings()
    settings.processArguments(scalacOptions.to[List], true)
    val classpath = artifacts.map(_.toAbsolutePath.toString).mkString(""+File.pathSeparatorChar)
    settings.bootclasspath.value = classpath
    settings.classpath.value = classpath
    settings.Yrangepos.value = true
    if(withoutParadisePlugin) {
      settings.plugin.value = settings.plugin.value.filterNot(_.contains("paradise"))
    }
    settings.copy
  }
}
