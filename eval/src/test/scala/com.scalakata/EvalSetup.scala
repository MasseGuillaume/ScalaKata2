package com.scalakata

trait EvalSetup {
  import scala.concurrent.duration._
  import java.nio.file.Path

  def wrap(code: String) = {
    """|import com.scalakata._
       |@instrumented object Playground {
       |  $code
       |}""".stripMargin
  }

  def eval(code: String) = compiler.eval(EvalRequest(wrap(code)))
  def autocomplete(code: String, pos: Int) = compiler.autocomplete(EvalRequest(wrap(code)))
  def typeAt(code: String, pos: Int) = compiler.typeAt(EvalRequest(wrap(code)))

  private val artifacts = build.BuildInfo.runtime_exportedProducts.map(Path.apply)
  private val scalacOptions = sbt.BuildInfo.scalacOptions.to[Seq]
  private def compiler = new Compiler(
    artifacts,
    scalacOptions,
    security = false,
    timeout = 20.seconds
  )
}