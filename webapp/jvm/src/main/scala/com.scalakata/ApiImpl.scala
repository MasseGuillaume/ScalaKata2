package com.scalakata

import evaluation._

import java.nio.file.Path
import scala.concurrent.duration._

class ApiImpl(artifacts: Seq[Path], scalacOptions: Seq[String], security: Boolean, timeout: FiniteDuration) extends Api {

  def create() = new Evaluator(artifacts, scalacOptions, security, timeout)
  private var eval = create()
  private val presentationCompiler = new PresentationCompiler(artifacts, scalacOptions)

  def autocomplete(request: CompletionRequest) = presentationCompiler.autocomplete(request)
  def eval(request: EvalRequest) = {
    val response1 = eval.apply(request)
    val response2 =
      if(paradiseCrash(response1)) {
        println("paradise crashed")
        eval = create()
        eval.apply(request)
      } else response1
    response2
  }
  def typeAt(request: TypeAtRequest) = presentationCompiler.typeAt(request)

  def paradiseCrash(response: EvalResponse) = {
    response.complilationInfos.get(Error).flatMap(_.headOption).map(_.message ==
      "macro annotation could not be expanded (the most common reason for that is that you need to enable the macro paradise plugin; another possibility is that you try to use macro annotation in the same compilation run that defines it)"
    ).getOrElse(false)
  }
}