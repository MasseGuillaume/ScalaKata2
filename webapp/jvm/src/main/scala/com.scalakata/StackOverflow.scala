package com.scalakata

import spray.http._
import spray.http.Uri._
import spray.client.pipelining._
import scala.concurrent.Future
import spray.json._

case class Response(items: List[Question])
case class Question(body: String, answers: List[Answers])
case class Answers(body: String)

object StackOverflowClient {
  import scala.concurrent.duration._
  import akka.util.Timeout

  import DefaultJsonProtocol._
  implicit val answersFormat = jsonFormat1(Answers)
  implicit val questionFormat = jsonFormat2(Question)
  implicit val reponseFormat = jsonFormat1(Response)

  def get(id: String)(implicit system: akka.actor.ActorRefFactory): Future[List[String]] = {
    implicit val timeout = Timeout(10.seconds)
    import system.dispatcher
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    // filter question body & awnser
    val filter = "!w-2aDJ0KX4MIH2KqTb"
    val uri = Uri("https://api.stackexchange.com/2.2/questions/" + id).copy(
      query = Query("site" -> "stackoverflow", "filter" -> filter)
    )
    pipeline(Get(uri)).map{
      case HttpResponse(StatusCodes.OK, entity: HttpEntity.NonEmpty, _, _) => {

        val response = entity.data.asString.parseJson.convertTo[Response]
        val bodies = response.items.flatMap(question =>  question.body +: question.answers.map(_.body) )
        bodies.map(StackOverflowParser.from)
      }
      case HttpResponse(StatusCodes.NotFound, _, _, _) => List("// Not Found")
    }
  }
}

object StackOverflowParser {
  def from(htmlSource: String): String = {
    import scala.xml.{NodeSeq, Elem, Text, TopScope}
    import scala.io.Source
    import scala.xml.parsing.XhtmlParser

    val nl = System.lineSeparator
    val tq = "\"\"\""
    
    def packBy[A](ls: List[A])(f: (A, A) => Boolean): List[List[A]] = {
      if (ls.isEmpty) List(List())
      else {
        val (packed, next) = ls span(f(_,ls.head))
        if (next == Nil) List(packed)
        else packed :: packBy(next)(f)
      }
    }

    def top(n: NodeSeq) = {
      n(0).child.collect {
        case n: Elem => n.copy(scope = TopScope)
        case t: Text => t
      }
    }
      
    def pack(exprs: List[(String, String)]) = {
      packBy(exprs)(_._1 == _._1)
      .map(ss => {
        val t = ss.head._1
        if(t == "text") {
          val s = "md" + tq
          val tr =
            if(ss.head._2.forall(_ == '\n')) ss.drop(1)
            else ss
          tr.map(s => s._2.trim.split(nl).mkString(" ")).mkString(
            s + "|", 
            nl + (" " * s.length) + "|", 
            tq + ".stripMargin.fold"
          ).trim
        }
        else ss.map(_._2).mkString(nl).trim
      }).mkString(nl)
    }

    def convert(nodes: NodeSeq): List[(String, String)] = {
      nodes.flatMap{ n =>
        if(n.label == "h1") List(("text", "# " + n.text))
        else if(n.label == "h2") List(("text", "## " + n.text))
        else if(n.label == "h3") List(("text", "### " + n.text))
        else if(n.label == "h4") List(("text", "#### " + n.text))
        else if(n.label == "h5") List(("text", "##### " + n.text))
        else if(n.label == "h6") List(("text", "###### " + n.text))
        else if(n.label == "p") {
          List(("text", top(n).map{ v =>
            if(v.label == "code") "`" + v.text + "`"
            else if(v.label == "a") {
              if((v \ "@href").text != "") "[" + v.text + "](" + (v \ "@href").text + ")"
              else v.toString
            }
            else if(v.label == "i") "*" + v.text + "*"
            else if(v.label == "b") "**" + v.text + "**"
            else v.text
          }.mkString("").trim))
        } else if(n.label == "div") {
          val clazz = (n \ "@class").text
          if(clazz == "figure") {
            val v = top(n)
            val a = v \\ "a"

            val img = v \\ "img"
            val alt = (img \ "@alt").text
            val src = (img \ "@src").text

            List(("text",
              s"""|$a
                  |![$alt]($src)""".stripMargin))
          } else if((n \ "@style").text == "page-break-after:always") Nil
          else if(clazz == "aside") {
            List(("text", 
              s"""|<div class="aside">
                  |${convert(top(n)).map(_._2).mkString(nl)}
                  |</div>""".stripMargin
            ))
          } else List(("text", n.text))         
        } else if(n.label == "ol") List(("text",
          top(n).zipWithIndex.map{ case(v,i) => s"""$i. ${v.text.trim.replace(nl, " ")}"""}.mkString(nl)
        ))
        else if(n.label == "ul") List(("text", 
          top(n).map(v => s"""* ${v.text.trim.replace(nl, " ")}""").mkString(nl)
        ))
        else if(n.label == "pre") {
          val replPrompt = "scala>"
          val resPrompt = "res"
          val vv =
            n.text.trim.split(nl).flatMap{ l =>
              if(l.startsWith(replPrompt)) List(l.drop(replPrompt.length + 1))
              else if(l.dropWhile(_ == ' ').startsWith(resPrompt)) Nil
              else List(l)
            }.mkString(nl)
          List(("code", vv))
        }
        else if(n.text != "") List(("text", n.text))
        else List(("text", ""))
      }.toList
    }

    val html = XhtmlParser(Source.fromString("<html><body>" + htmlSource.replaceAll(raw"\\n", nl) + "</body></html>"))
    pack(convert(top(html \\ "html" \\ "body")))
  }
}