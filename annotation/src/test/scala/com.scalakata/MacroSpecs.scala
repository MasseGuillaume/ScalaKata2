package com.scalakata

class MacroSpecs extends org.specs2.Specification { def is = s2"""
  Kata Macro Specifications
    var/val $varVal
    full $full
    val extraction $extraction
    typesplit $typesplit
    hide instrumented class name $hideName
"""

  def s(v: String) = "\"" + v + "\""

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
      RangePosition( 8,  8, 13) -> Value("2", "Int"),
      RangePosition(22, 22, 27) -> Value("4", "Int")
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
      RangePosition(  8,   8,  13) -> Value(s("L29")        , "String"),
      RangePosition( 22,  22,  27) -> Value(s("L30")        , "String"),
      RangePosition( 28,  28,  33) -> Value(s("L29L30")     , "String"),
      RangePosition( 42,  42,  47) -> Value(s("L29L30")     , "String"),
      RangePosition( 49,  49,  56) -> Value("Set(33)"       , "Set[Int]"),
      RangePosition(151, 151, 160) -> Value(s("L34v")       , "String"),
      RangePosition(175, 175, 176) -> Value(s("L40")        , "String"),
      RangePosition(177, 177, 225) -> Value(s("L42-1L42-2") , "String"),
      RangePosition(229, 229, 263) -> Value("true"          , "Boolean"),
      RangePosition(272, 272, 277) -> Value(s("L45")        , "String"),
      RangePosition(282, 282, 287) -> Value(s("L46")        , "String"),
      RangePosition(296, 296, 327) -> Value("Map(1 -> 47)"  , "collection.mutable.Map[Int, Int]"),
      RangePosition(335, 335, 337) -> Value("48"            , "Int"),
      RangePosition(339, 339, 392) -> Value(s("535454")     , "String"),
      RangePosition(437, 437, 444) -> Value(s("L56-t")      , "String"),
      RangePosition(467, 467, 471) -> Value("null"          , "Null")
    )
  }

  def extraction = {
@instrument class Extraction {
  val (a, b) = (1, 2)
}

    withOffset(new Extraction) ====  List(
      (RangePosition(7,7,7), Value("1", "Int")),
      (RangePosition(10,10,10), Value("2", "Int"))
    )
  }

  def typesplit = {
@instrument class TypeSplit {
  val withDefault: Option[Int] ⇒ Int = {
    case Some(x) ⇒ x
    case None ⇒ 0
  }
}

    withOffset(new TypeSplit) ====
      List((RangePosition(39,39,83),Value("<function1>","Option[Int] => Int")))
  }

  def hideName = {
@instrument class FooBar {
  trait B
  case class A() extends B
  def a: B = A()
  a
}

    withOffset(new FooBar) ====
      List((RangePosition(56,56,57),Value("A()","B")))
  }
}