package com

package object scalakata {
  implicit val rangePositionOrdering: Ordering[RangePosition] =
    Ordering.by(RangePosition.unapply)

  type Instrumentation = List[(RangePosition, String)]
  def show[T](v: T) = if(v != null) v.toString else "null"
  def println[T](v: T) = show[T](v)
}