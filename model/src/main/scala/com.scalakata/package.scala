package com

package object scalakata {
  type Instrumentation = List[((Int, Int), String)]
  def show[T](v: T) = if(v != null) v.toString else "null"
  def println[T](v: T) = show[T](v)
}