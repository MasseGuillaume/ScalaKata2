package com.scalakata

import scala.collection.mutable.{Map => MMap}

class MacroTest extends org.specs2.Specification { def is = s2"""
  Kata Macro Specification
    var/val $varVal
    full $full
"""

  def show[T](v: T) = if(v != null) v.toString else "null"
  def println[T](v: T) = show[T](v)

  def varVal = {

@instrument object VarVal {
val a = 1 + 1
var b = 2 + 2
}

    VarVal.instrumentation$ ====  List(
      ( 8,13) -> "2",
      (22,27) -> "4"
    )
  }

  def full = {

@instrument object Full {
val a = "L29"
var b = "L30"
a + b
println(a + b)
Set(33)
case class L34(b: Int){ def v = "L34v" }
class L35
trait L36
object L37
type L38[V] = List[V]
L34(39).v
def f = "L40"
f
("L42-1", "L42-2") match { case (a, b) => a + b }
//
implicitly[Ordering[Int]].lt(1, 2)
var d = "L45"
d = "L46"
val m = collection.mutable.Map(1 -> 47)
m(1) = 48
"L49": String

{
  val a = "53"
  val b = "54"
  a + b
  a + b + b
}
while(d == "L46") {
  d = "L56"
}

if(true) "L56-t" else "L56-f"
if(true) null
}

    Full.instrumentation$ ==== List(
       ( 8, 13) -> "L29",
       (22, 27) -> "L30",
       (28, 33) -> "L29L30",
       (34, 48) -> "L29L30",
       (49, 56) ->  "Set(33)",
      (151,160) -> "L34v",
      (175,176) -> "L40",
      (177,226) -> "L42-1L42-2",
      (230,264) -> "true",
      (273,278) -> "L45",
      (283,288) -> "L46",
      (297,328) -> "Map(1 -> 47)",
      (336,338) -> "48",
      (339,352) -> "L49",
      (354,407) -> "535454",
      (452,459) -> "L56-t",
      (482,486) -> "null"
    )

  }
}