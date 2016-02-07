package com.scalakata

import scala.collection.mutable.{Map ⇒ MMap}

class MacroSpecs extends org.specs2.Specification { def is = s2"""
  Kata Macro Specifications
    full $full
"""

  def withOffset(instr: Instrumented) = {
    val by = instr.offset$
    instr.instrumentation$.map{ case
     
     (RangePosition(start, pos, end), repr) ⇒
     
      
      (RangePosition(start - by, pos - by, end - by), repr)
    }
  }

  def full = {

@instrument class Full {
  val a: Option[Int] = Some(1)
  a match { case None => 1; case Some(v) => v}
  1.0: Double
  for { v <- Some(1) } yield v
  identity(1)
  List.empty
  a;
  {a; a}
  try { 1 / 0 } catch { case scala.util.control.NonFatal(e) => 0 }
}
    withOffset(new Full) ==== List(
      (RangePosition(33,33,77),   Value("1","Int")),
      (RangePosition(80,80,91),   Value("1.0","Double")),
      (RangePosition(94,94,122),  Value("Some(1)","scala.Option[Int]")),
      (RangePosition(125,125,136),Value("1","Int")),
      (RangePosition(139,139,149),Value("List()","scala.collection.immutable.List[Nothing]")),
      (RangePosition(152,152,153),Value("Some(1)","scala.Option[Int]")),
      (RangePosition(157,157,163),Value("Some(1)","scala.Option[Int]")),
      (RangePosition(166,166,230),Value("0","Int"))
    )
  }
}