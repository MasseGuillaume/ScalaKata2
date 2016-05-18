package com.scalakata
package evaluation

import java.nio.file.Path
import scala.tools.nsc.interactive.Global

import scala.tools.nsc.reporters.StoreReporter
import scala.reflect.internal.util._
import scala.tools.nsc.interactive.Response

class PresentationCompiler(artifacts: Seq[Path], scalacOptions: Seq[String]) {

  def autocomplete(request: CompletionRequest): List[CompletionResponse] = {
    def completion(f: (compiler.Position, compiler.Response[List[compiler.Member]]) ⇒ Unit,
                   pos: compiler.Position):
                   List[CompletionResponse] = {

      withResponse[List[compiler.Member]](r ⇒ f(pos, r)).get match {
        case Left(members) ⇒ compiler.ask(() ⇒ {
          members.sortBy(m ⇒ (!m.sym.isPublic, m.sym.decodedName)).map(member ⇒
            CompletionResponse(
              name = member.sym.decodedName,
              signature = compiler.showDecl(member.sym)
            )
          )
        })
        case Right(e) ⇒
          e.printStackTrace
          Nil
      }
    }
    def typeCompletion(pos: compiler.Position) = {
      completion(compiler.askTypeCompletion _, pos)
    }

    def scopeCompletion(pos: compiler.Position) = {
      completion(compiler.askScopeCompletion _, pos)
    }

    // inspired by scala-ide
    // https://github.com/scala-ide/scala-ide/blob/49ab209db4f54bd08824bf91d449df736b206f58/org.scala-ide.sdt.core/src/org/scalaide/core/completion/ScalaCompletions.scala#L179
    askTypeAt(request.code, request.position) { (tree, pos) ⇒ tree match {
      case compiler.New(name) ⇒ typeCompletion(name.pos)
      case compiler.Select(qualifier, _) if qualifier.pos.isDefined && qualifier.pos.isRange ⇒
        typeCompletion(qualifier.pos)
      case compiler.Import(expr, _) ⇒ typeCompletion(expr.pos)
      case compiler.Apply(fun, _) ⇒
        fun match {
          case compiler.Select(qualifier: compiler.New, _) ⇒ typeCompletion(qualifier.pos)
          case compiler.Select(qualifier, _) if qualifier.pos.isDefined && qualifier.pos.isRange ⇒
            typeCompletion(qualifier.pos)
          case _ ⇒ scopeCompletion(fun.pos)
        }
      case _ ⇒ scopeCompletion(pos)
    }}{
      pos ⇒ Some(scopeCompletion(pos))
    }.getOrElse(Nil)
  }

  def typeAt(request: TypeAtRequest): Option[TypeAtResponse] = {
    askTypeAt(request.code, request.position){(tree, _) ⇒ {
      // inspired by ensime
      // https://github.com/ensime/ensime-server/blob/dc0c682854d6210010069c062d9fb8cf3d7707b2/core/src/main/scala/org/ensime/core/RichPresentationCompiler.scala#L333
      val res =
        tree match {
          case compiler.Select(qual, name) ⇒ qual
          case t: compiler.ImplDef if t.impl != null ⇒ t.impl
          case t: compiler.ValOrDefDef if t.tpt != null ⇒ t.tpt
          case t: compiler.ValOrDefDef if t.rhs != null ⇒ t.rhs
          case t ⇒ t
        }
      TypeAtResponse(res.tpe.toString)
    }}{_ => None}
  }

  private def askTypeAt[A]
    (code: String, position: com.scalakata.RangePosition)
    (f: (compiler.Tree, compiler.Position) ⇒ A)
    (fb: compiler.Position ⇒ Option[A]): Option[A] = {

    if(code.isEmpty) None
    else {
      val file = reload(code)
      val rpos = compiler.rangePos(file, position.start, position.point, position.end)

      val response = withResponse[compiler.Tree](r ⇒ compiler.askTypeAt(rpos, r))

      response.get match {
        case Left(tree) ⇒ Some(f(tree, rpos))
        case Right(e) ⇒ e.printStackTrace; fb(rpos)
      }
    }
  }

  private def reload(code: String): BatchSourceFile = {
    val file = new BatchSourceFile("default", code)
    withResponse[Unit](r ⇒ compiler.askReload(List(file), r)).get
    file
  }

  private def withResponse[A](op: Response[A] ⇒ Any): Response[A] = {
    val response = new Response[A]
    op(response)
    response
  }

  private val reporter = new StoreReporter()
  private val settings = toSettings(artifacts, scalacOptions, withoutParadisePlugin = true)
  private val compiler = new Global(settings, reporter)
}