package com.scalakata

import scala.collection.mutable.{Map => MMap}

class MacroSpecs extends org.specs2.Specification { def is = s2"""
  Kata Macro Specifications
    var/val $varVal
    full $full
"""

  def withOffset(instr: Instrumented) = {
    val by = instr.offset$
    instr.instrumentation$.map{ case (RangePosition(start, pos, end), repr) =>
      (RangePosition(start - by, pos - by, end - by), repr)
    }
  }

  def varVal = {

@instrument class VarVal {
val a = 1 + 1
var b = 2 + 2
}

    withOffset(new VarVal) ====  List(
      RangePosition(8, 8, 13) -> "2",
      RangePosition(22, 22, 27) -> "4"
    )
  }

  def full = {

@instrument class Full {
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

    withOffset(new Full) ==== List(
      RangePosition(  8,   8,  13) -> "L29",
      RangePosition( 22,  22,  27) -> "L30",
      RangePosition( 28,  28,  33) -> "L29L30",
      RangePosition( 34,  34,  48) -> "L29L30",
      RangePosition( 49,  49,  56) -> "Set(33)",
      RangePosition(151, 151, 160) -> "L34v",
      RangePosition(175, 175, 176) -> "L40",
      RangePosition(177, 177, 226) -> "L42-1L42-2",
      RangePosition(230, 230, 264) -> "true",
      RangePosition(273, 273, 278) -> "L45",
      RangePosition(283, 283, 288) -> "L46",
      RangePosition(297, 297, 328) -> "Map(1 -> 47)",
      RangePosition(336, 336, 338) -> "48",
      RangePosition(340, 340, 393) -> "535454",
      RangePosition(438, 438, 445) -> "L56-t",
      RangePosition(468, 468, 472) -> "null"
    )
  }
}