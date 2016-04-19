package com.scalakata

import language.experimental.macros

object KataMacro {
  def instrument(c: reflect.macros.whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Instrumented] = {
    import c.universe._

    def instrumentOne(tree: Tree, instrumentation: TermName) = {
      implicit def liftq = Liftable[c.universe.Position] { p ⇒
        q"_root_.com.scalakata.RangePosition(${p.start}, ${p.start}, ${p.end})"
      }

      def w(aTree: Tree, tTree: Option[Tree] = None) = {
        val t = TermName(c.freshName)
        val pp = TermName(c.freshName)

        if(aTree.pos == NoPosition) aTree
        else {
          val tTreeQuote =
            tTree match {
              case None      ⇒ q"val $t = $aTree"
              case Some(tpe) ⇒ q"val $t: $tpe = $aTree"
            }
          q"""{
            $tTreeQuote
            ${instrumentation}(${aTree.pos}) = render($t, pprint.tokenize($t).mkString)
            $t
          }"""
        }
      }

      def extract(vd: ValDef) = {
        val ValDef(mods, name, tpt, expr) = vd

        // val (a, b) = (1, 2) would add a SYNTHETIC tree, we dont instrument it
        if(mods.hasFlag(Flag.SYNTHETIC)) vd
        else ValDef(mods, name, tpt, w(expr, Some(tpt)))
      }

      // see http://docs.scala-lang.org/overviews/quasiquotes/syntax-summary.html
      tree match {
        case q"println {..$body}"              ⇒ w(q"{..$body}")
        case q"$expr(..$exprs) = $rhs"         ⇒ q"$expr(..$exprs) = ${w(rhs)}"
        case q"if ($cond) $texpr else $fexpr"  ⇒ q"if ($cond) ${w(texpr)} else ${w(fexpr)}"
        case q"for (..$enums) yield $expr"     ⇒ w(tree)
        case q"while ($_) $_"                  ⇒ tree
        case q"$expr1 = $expr2"                ⇒ q"$expr1 = ${w(expr2)}"
        case q"$_ match { case ..$_ }"         ⇒ w(tree)
        case q"$expr[..$tpts]"                 ⇒ w(tree)                                      // implicitly[Ordering[Int]]
        case q"$expr: $tpt"                    ⇒ w(tree, Some(tpt))                           // a: Int
        case q"$expr match { case ..$cases }"  ⇒ w(tree)
        case q"while ($cond) $expr"            ⇒ tree
        case q"do $cond while ($expr)"         ⇒ tree
        case q"for (..$enums) $expr"           ⇒ tree
        case vd: ValDef                        ⇒ extract(vd)                                  // var / val
        case _: Apply                          ⇒ w(tree)                                      // f(1)
        case _: Select                         ⇒ w(tree)                                      // p.x
        case _: Ident                          ⇒ w(tree)                                      // p
        case b: Block                          ⇒ w(tree)                                      // {a; b}
        case _: Try                            ⇒ w(tree)                                      // try ...
        case lit: Literal                      ⇒ lit                                          // 1.0
        case ld: LabelDef                      ⇒ ld                                           // do / while
        case cd: ClassDef                      ⇒ cd                                           // class A / trait A
        case md: ModuleDef                     ⇒ md                                           // object A
        case td: TypeDef                       ⇒ td                                           // type A = List
        case dd: DefDef                        ⇒ dd                                           // def f = 1
        case im: Import                        ⇒ im
        case v                                 ⇒ { println(showRaw(v)); v }
      }
    }

    
    c.Expr[Instrumented]{
      annottees.map(_.tree).toList match { case q"class $name { ..$body }" :: Nil ⇒
        val instrumentation = TermName(c.freshName)

        val offset = 
          c.enclosingPosition.end + (
          " " +
          s"""|class $name {
              |""".stripMargin).length

        try {
          q"""
          class $name extends Instrumented {
            private val $instrumentation = scala.collection.mutable.Map[_root_.com.scalakata.RangePosition, Render]()
            def offset$$ = $offset
            def instrumentation$$: _root_.com.scalakata.Instrumentation = ${instrumentation}.toList.sorted
            ..${body.map(t ⇒ instrumentOne(t, instrumentation))}
          }
          """
        } catch {
          case scala.util.control.NonFatal(e) ⇒ {
            println(s"compiler bug ${e.toString}")
            q"""
            class $name extends Instrumented {
              private val $instrumentation = scala.collection.mutable.Map[_root_.com.scalakata.RangePosition, Render]()
              def offset$$ = $offset
              def instrumentation$$: _root_.com.scalakata.Instrumentation = ${instrumentation}.toList.sorted
            }
            """
          }
        }
      }
    }
  }
}

trait Instrumented {
  def instrumentation$: Instrumentation
  def offset$: Int
}

class instrument extends annotation.StaticAnnotation {
  def macroTransform(annottees: Any*): Instrumented = macro KataMacro.instrument
}