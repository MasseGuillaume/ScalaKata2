package com.scalakata

import scala.collection.mutable.{Map => MMap}

class MacroTest extends org.specs2.Specification { def is = s2"""
  Kata Macro Specification
    var/val $varVal
    full $full
"""
  def show[T](v: T) = v.toString
  def println[T](v: T) = show[T](v)

  def varVal = {

@kata object VarVal {
val a = 1 + 1
var b = 2 + 2
}
    
    VarVal.scalakata$ ==== MMap(
      (0, 13) -> "2",
      (14, 27) -> "4"
    )
  }

  def full = {

@kata object Full {
val a = 1 + 1
var b = 2 + 2
a + b
println(a - b)
Set(1)
case class A(b: String){ def v = 1 }
class B
trait C
object D
type E[V] = List[V]
A("a").v
def f = 42
f
(1, 2) match { case (a, b) => a + b }
42
implicitly[Ordering[Int]].lt(1, 2)
var d = 1 + 5
d = 1 + 1
val m = collection.mutable.Map(1 -> 1)
m(1) = -1
"a": String

{
  '+'
  '='
}

if(true) 1 else 0
}

    Full.scalakata$ ==== MMap(
      (0, 13) -> "2",
      (14, 27) -> "4",
      (28, 33) -> "6",
      (34, 48) -> "-2",
      (49, 55) -> "Set(1)",
      (138, 146) -> "1",
      (158, 159) -> "42",
      (160, 197) -> "3",
      (201, 235) -> "true",
      (236, 249) -> "6",
      (250, 259) -> "2",
      (260, 298) -> "Map(1 -> 1)",
      (299, 308) -> "-1",
      (309, 320) -> "a",
      (322, 337) -> "=",
      (326, 329) -> "+"

    )
  }
}