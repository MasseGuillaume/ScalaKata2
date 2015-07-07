package com.scalakata

trait Api{
  def eval(request: EvalRequest): EvalResponse
  def typeAt(request: TypeAtRequest): Option[TypeAtResponse]
  def autocomplete(request: CompletionRequest): List[CompletionResponse]
}