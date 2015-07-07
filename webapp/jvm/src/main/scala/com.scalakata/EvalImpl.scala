package com.scalakata

import scala.concurrent.duration._

trait EvalImpl extends Api {
  val artifacts: String
  val scalacOptions: Seq[String]
  val security: Boolean
  val timeout: Duration

  lazy val compiler = new com.scalakata.Compiler(artifacts, scalacOptions, security, timeout)

  def autocomplete(request: CompletionRequest) = compiler.autocomplete(request)
  def eval(request: EvalRequest) = compiler.eval(request)
  def typeAt(request: TypeAtRequest) = compiler.typeAt(request)
}