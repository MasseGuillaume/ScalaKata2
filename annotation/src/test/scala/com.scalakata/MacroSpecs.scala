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

  // case q"$_ match { case ..$_ }" ⇒ w(tree)
  // case q"$expr[..$tpts]"         ⇒ w(tree)   // implicitly[Ordering[Int]]
  // case q"$expr: $tpt"            ⇒ w(tree)   // a: Int
  // case q"for (..$enums) $expr"   ⇒ tree
  // case _: Apply                  ⇒ w(tree)   // f(1)
  // case _: Select                 ⇒ w(tree)   // p.x
  // case _: Ident                  ⇒ w(tree)   // p
  // case _: Block                  ⇒ w(tree)   // {a; b}
  // case _: Try                    ⇒ w(tree)   // try ...
  // case v                         ⇒ v

  def full = {

@instrument class Full {
  val a: Option[Int] = Some(1)
  a match { case None => 1; case Some(v) => v}
  implicitly[Ordering[Int]]
  1.0: Double
  for { v <- Some(1) } yield v
  identity(1)
  List.empty
  a;
  {a; a}
  try { 1 / 0 } catch { case scala.util.control.NonFatal(e) => 0 }
}
    withOffset(new Full) ==== List(
      (RangePosition( 33, 33, 77), Value("1","Int")),
      (RangePosition( 80, 80,105), Value("scala.math.Ordering$Int$@2a52bf21","scala.math.Ordering[Int]")),
      (RangePosition(108,108,119), Value("1.0","Double")),
      (RangePosition(122,122,150), Value("Some(1)","scala.Option[Int]")),
      (RangePosition(153,153,164), Value("1","Int")),
      (RangePosition(167,167,177), Value("List()","scala.collection.immutable.List[Nothing]")),
      (RangePosition(180,180,181), Value("Some(1)","scala.Option[Int]")),
      (RangePosition(185,185,191), Value("Some(1)","scala.Option[Int]")),
      (RangePosition(194,194,258), Value("0","Int"))
    )
  }
}