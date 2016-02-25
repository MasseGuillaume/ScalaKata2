package com.scalakata

import scala.concurrent.Future

trait Api{
  def eval(request: EvalRequest): EvalResponse
  def typeAt(request: TypeAtRequest): Option[TypeAtResponse]
  def autocomplete(request: CompletionRequest): List[CompletionResponse]
}

object Util {
  val nl = '\n'
  val prelude = 
    """|import com.scalakata._
       |
       |@instrument class Playground {
       |  """.stripMargin
    
  def wrap(code: String): String = prelude + code + nl + "}"
}