package com.scalakata

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.Dynamic.global
import scalajs.concurrent.JSExecutionContext.Implicits.runNow

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLTextAreaElement

import scalatags.JsDom.all._
import upickle._
import autowire._

import scala.util.Random
import scala.concurrent.Future

import org.denigma.codemirror._


object Client extends autowire.Client[String, upickle.Reader, upickle.Writer]{
  override def doCall(req: Request): Future[String] = {
    dom.ext.Ajax.post(
      url = "/api/" + req.path.mkString("/"),
      data = upickle.write(req.args)
    ).map(_.responseText)
  }

  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}


object Parsing {
  def apply(code: String) = {
    import fastparse.parsers.Combinators.Rule
    import fastparse.core._
    import scalaparse._
    import scalaparse.Scala._
     
    var indices = collection.mutable.Buffer((0, ""))
    var done = false

    val tokens: PartialFunction[Rule[_], String] = {
      case Literals.Expr.Interp | Literals.Pat.Interp => "RESET"
      case Literals.Comment => "BLUE"
      case ExprLiteral => "GREEN"
      case TypeId => "GREEN"
    }
    
    
    Scala.CompilationUnit.parse(
      input = code,
      index = 0,
      instrument = (rule: Parser[_], idx: Int, res: () => Result[_]) => {
        val (lastIdx, lastRule) = indices.last
        val startIndex = indices.length
        tokens.lift(rule.asInstanceOf[Rule[_]]).map{ r =>
          if(idx >= lastIdx && r != lastRule) {
            indices += ((idx, r))
            res() match {
              case s: Result.Success[_] => indices += ((s.index, lastRule))
              case _ => ()
            }
          }
        }
        
      }
    ) match {
      case f: Result.Failure => global.console.log(f.toString)
      case s: Result.Success[_] => global.console.log(indices.drop(1).toString)
    }

    indices
  }
}

@JSExport
object ScalaJSExample {
  @JSExport
  def main(): Unit = {

    var code = "object A { def v = 1 }"

    CodeMirror.defineMode("scala", (config: js.Object, parserConfig: js.Object) => {
      js.Dynamic.literal(
        "startState" -> { () =>
          0
        }
        "token" -> {(stream: LineStream, state: Int) => {
          dom.console.log("code:" + code)
          ""
        }}
      )
    })
    val params = EditorConfig.mode("scala").lineNumbers(true)
    dom.document.getElementById("scalakata") match {
      case el:HTMLTextAreaElement ⇒
        val editor = CodeMirror.fromTextArea(el, params)
        editor.getDoc().setValue(code)
        editor.on("change", (e: Editor) => {
          code = e.getDoc.getValue()
        })
      case _ ⇒ dom.console.error("cannot find text area for the code!")
    }
  }
}
