package com.scalakata

trait Api{
  def eval(code: String): Map[(Int, Int), String]
}
