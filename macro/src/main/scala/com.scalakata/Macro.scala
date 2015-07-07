package com.scalakata

import language.experimental.macros

object KataMacro {
  def instrument(c: reflect.macros.whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Instrumented] = {
    import c.universe._

    def instrumentOne(tree: Tree, instrumentation: TermName, offset: Int) = {
      implicit def liftq = Liftable[c.universe.Position] { p ⇒
        q"(${p.start - offset}, ${p.end - offset})"
      }

      def w(aTree: Tree) = {
        val t = TermName(c.freshName)
        q"""{
          val $t = $aTree
          ${instrumentation}(${aTree.pos}) = show($t)
          $t
        }"""
      }

      // see http://docs.scala-lang.org/overviews/quasiquotes/syntax-summary.html
      tree match {
        case q"$expr(..$exprs) = $rhs"         => q"$expr(..$exprs) = ${w(rhs)}"
        case q"if ($cond) $texpr else $fexpr"  => q"if ($cond) ${w(texpr)} else ${w(fexpr)}"
        case q"for (..$enums) yield $expr"     => q"for (..$enums) yield ${w(expr)}"
        case q"while ($_) $_"                  => tree
        case q"$expr1 = $expr2"                => q"$expr1 = ${w(expr2)}"
        case q"$_ match { case ..$_ }"         => w(tree)
        case q"$expr[..$tpts]"                 => w(tree)                            // implicitly[Ordering[Int]]
        case q"$expr: $tpt"                    => w(tree)                            // a: Int
        case q"$expr match { case ..$cases }"  => w(tree)
        case q"while ($cond) $expr"            => tree
        case q"do $cond while ($expr)"         => tree
        case q"for (..$enums) $expr"           => tree
        case ValDef(mods, tname, tpt, expr)    => ValDef(mods, tname, tpt, w(expr))  // var / val
        case _: Apply                          => w(tree)                            // f(1)
        case _: Select                         => w(tree)                            // p.x
        case _: Ident                          => w(tree)                            // p
        case _: Block                          => w(tree)                            // {a; b}
        case _: Try                            => w(tree)                            // try ...
        case lit: Literal                      => lit                                // 1.0
        case ld: LabelDef                      => ld                                 // do / while
        case cd: ClassDef                      => cd                                 // class A / trait A
        case md: ModuleDef                     => md                                 // object A
        case td: TypeDef                       => td                                 // type A = List
        case dd: DefDef                        => dd                                 // def f = 1
        case v                                 => { println(showRaw(v)); v }
      }
    }

    c.Expr[Instrumented]{
      annottees.map(_.tree).toList match { case q"object $name { ..$body }" :: Nil ⇒
        val instrumentation = TermName(c.freshName)

        val offset = 
          c.enclosingPosition.end + (
          " " +
          s"""|object $name {
              |""".stripMargin).length

        q"""
        object $name extends Instrumented {
          private val $instrumentation = scala.collection.mutable.Map[(Int, Int), String]()
          def instrumentation$$: com.scalakata.Instrumentation = ${instrumentation}.toList.sorted
          ..${body.map(t => instrumentOne(t, instrumentation, offset))}
        }
        """
      }
    }
  }
}

trait Instrumented {
  def instrumentation$: Instrumentation
}

class instrument extends annotation.StaticAnnotation {
  def macroTransform(annottees: Any*): Instrumented = macro KataMacro.instrument
}