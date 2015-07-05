package com.scalakata

import scala.reflect.macros.whitebox.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation

object KataMacro {
  def instrumentation(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    c.Expr[Any]{
      annottees.map(_.tree).toList match {
        case q"object $name { ..$body }" :: Nil ⇒
          val instr = TermName("scalakata$")

          val offset = 
            c.enclosingPosition.end + (
            " " +
            s"""|object $name {
                |""".stripMargin).length

          implicit def liftq = Liftable[c.universe.Position] { p ⇒
            q"(${p.start - offset}, ${p.end - offset})"
          }

          def instrumentOne(tree: Tree) = {
            def wrap(aTree: Tree = tree)(pTree: Tree = tree) = {
              val t = TermName(c.freshName)
              q"""{
                val $t = $aTree
                ${instr}(${pTree.pos}) = show($t)
                $t
              }"""
            }

            tree match {
              case q"$expr(..$exprs) = $rhs" => q"$expr(..$exprs) = ${wrap(rhs)()}" //  todo update params
              case ValDef(mod, name, tpe, rhs) => ValDef(mod, name, tpe, wrap(rhs)())
              case ap: Apply => wrap(ap)()
              case cd: ClassDef => cd
              case md: ModuleDef => md
              case sel: Select => wrap(sel)()
              case td: TypeDef => td
              case dd: DefDef => dd
              case id: Ident => wrap(id)()
              case m: Match => wrap(m)()
              case lit: Literal => lit
              case tap: TypeApply => wrap(tap)()
              case Assign(id, rhs) => Assign(id, wrap(rhs)())
              case t: Typed => wrap(t)()
              case Block(stats, last) =>  Block(stats.map(s => {wrap(s)(s)}), wrap(last)())
              case v => {
                println(showRaw(v))
                v
              }
            }
          }

          q"""
          object $name {
            val $instr = scala.collection.mutable.Map[(Int, Int), String]()
            ..${body.map(t => instrumentOne(t))}
          }
          """
      }
    }
  }
}

class kata extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro KataMacro.instrumentation
}