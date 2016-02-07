package com.scalakata

import scala.concurrent.duration._

import java.nio.file.Path

trait EvalImpl extends Api {
  val artifacts: Seq[Path]
  val scalacOptions: Seq[String]
  val security: Boolean
  val timeout: Duration
  val prod: Boolean


  implicit val system: akka.actor.ActorRefFactory

  lazy val compiler = new com.scalakata.Compiler(artifacts, scalacOptions, security, timeout)

  def autocomplete(request: CompletionRequest) = compiler.autocomplete(request)
  def eval(request: EvalRequest) = compiler.eval(request)
  def typeAt(request: TypeAtRequest) = compiler.typeAt(request)
}