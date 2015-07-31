package com.scalakata

import scala.collection.mutable.{Map ⇒ MMap}

class MacroSpecs extends org.specs2.Specification { def is = s2"""
  Kata Macro Specifications
    var/val $varVal
    full $full
"""

  def withOffset(instr: Instrumented) = {
    val by = instr.offset$
    instr.instrumentation$.map{ case (RangePosition(start, pos, end), repr) ⇒
      (RangePosition(start - by, pos - by, end - by), repr)
    }
  }

  def varVal = {

@instrument class VarVal {
val a = 1 + 1
var b = 2 + 2
}

    withOffset(new VarVal) ====  List(
      RangePosition( 8,  8, 13) -> Other("2"),
      RangePosition(22, 22, 27) -> Other("4")
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
("L42-1", "L42-2") match { case (a, b) ⇒ a + b }
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
      RangePosition(  8,   8,  13) -> EString("L29"),
      RangePosition( 22,  22,  27) -> EString("L30"),
      RangePosition( 28,  28,  33) -> EString("L29L30"),
      RangePosition( 42,  42,  47) -> EString("L29L30"),
      RangePosition( 49,  49,  56) ->   Other("Set(33)"),
      RangePosition(151, 151, 160) -> EString("L34v"),
      RangePosition(175, 175, 176) -> EString("L40"),
      RangePosition(177, 177, 225) -> EString("L42-1L42-2"),
      RangePosition(229, 229, 263) ->   Other("true"),
      RangePosition(272, 272, 277) -> EString("L45"),
      RangePosition(282, 282, 287) -> EString("L46"),
      RangePosition(296, 296, 327) ->   Other("Map(1 -> 47)"),
      RangePosition(335, 335, 337) ->   Other("48"),
      RangePosition(339, 339, 392) -> EString("535454"),
      RangePosition(437, 437, 444) -> EString("L56-t"),
      RangePosition(467, 467, 471) ->   Other("null")
    )
  }
}