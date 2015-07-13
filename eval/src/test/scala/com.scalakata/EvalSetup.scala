package com.scalakata

trait EvalSetup {
  import scala.concurrent.duration._
  import java.nio.file.Path

  private val prelude =
    """|import com.scalakata._
       |@instrumented object Playground {
       |  """.stripMargin
  private def wrap(code: String) = s"$prelude$code}"
  private def shiftRequest(pos: Int) = {
    val posO = pos + prelude.length
    RangePosition(posO, posO, posO)
  }

  def eval(code: String) = {
    compiler.eval(EvalRequest(wrap(code)))
  }
  def autocomplete(code: String, pos: Int) = {
    compiler.autocomplete(EvalRequest(wrap(code), shiftRequest(pos)))
  }
  def typeAt(code: String, pos: Int) = {
    compiler.typeAt(TypeAtRequest(wrap(code), shiftRequest(pos)))
  }

  private val artifacts = build.BuildInfo.runtime_exportedProducts.map(Path.apply)
  private val scalacOptions = sbt.BuildInfo.scalacOptions.to[Seq]
  private val compiler = new Compiler(
    artifacts,
    scalacOptions,
    security = false,
    timeout = 20.seconds
  )
}