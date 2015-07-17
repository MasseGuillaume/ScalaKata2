package com.scalakata

trait EvalSetup {
  import scala.concurrent.duration._
  import java.nio.file.Paths

  private val prelude =
    """|import com.scalakata._
       |@instrument class Playground {
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
    compiler.autocomplete(CompletionRequest(wrap(code), shiftRequest(pos)))
  }
  def typeAt(code: String, pos: Int) = {
    compiler.typeAt(TypeAtRequest(wrap(code), shiftRequest(pos)))
  }

  private val artifacts = build.BuildInfo.runtime_fullClasspath.map(v => Paths.get(v.toURI))

  private val scalacOptions = build.BuildInfo.scalacOptions.to[Seq]
  private def compiler = new Compiler(
    artifacts,
    scalacOptions,
    security = false,
    timeout = 30.seconds
  )
}