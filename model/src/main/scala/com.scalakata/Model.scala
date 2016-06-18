package com.scalakata

case class RangePosition(
  start: Int,
  point: Int,
  end: Int
)

sealed trait Severity
final case object Info extends Severity
final case object Warning extends Severity
final case object Error extends Severity

case class CompilationInfo(
  message: String,
  pos: Option[RangePosition]
)

// TODO: stacktrace
// stack: List[StackElement]
// String  getClassName()
// String  getFileName()
// int getLineNumber()
// String  getMethodName()
// TODO: range pos ?
case class RuntimeError(
  message: String,
  position: Option[Int]
)

// TODO: scalacOptions & dependencies
case class EvalRequest(
  code: String
)

sealed trait Render
case class Value(v: String, className: String) extends Render
case class Markdown(a: String, folded: Boolean = false) extends Render {
  def stripMargin = Markdown(a.stripMargin)
  def fold = copy(folded = true)
}
case class Html(a: String, folded: Boolean = false) extends Render {
  def stripMargin = copy(a = a.stripMargin)
  def fold = copy(folded = true)
}
case class Html2(a: String, folded: Boolean = false) extends Render {
  def stripMargin = copy(a = a.stripMargin)
  def fold = copy(folded = true)
}

case class EvalResponse(
  complilationInfos: Map[Severity, List[CompilationInfo]],
  timeout: Boolean,
  runtimeError: Option[RuntimeError],
  instrumentation: List[(RangePosition, Render)],
  console: String
)
object EvalResponse {
  val empty = EvalResponse(Map.empty, false, None, Nil, "")
}

// TODO: scalacOptions & dependencies
case class TypeAtRequest(
  code: String,
  position: RangePosition
)

case class TypeAtResponse(
  val tpe: String
)

// TODO: scalacOptions & dependencies
case class CompletionRequest(
  code: String,
  position: RangePosition
)

case class CompletionResponse(
  val name: String,
  signature: String
)

case class KeepAlive(msg: String = "") extends AnyVal

sealed trait RoomListEvent
case class NewRoom(roomName: String, user: String) extends RoomListEvent
case class CloseRoom(roomName: String) extends RoomListEvent
case class UpdateRoom(roomName: String, users: Vector[String]) extends RoomListEvent
case class SetRooms(rooms: Map[String, Vector[String]]) extends RoomListEvent

sealed trait CollaborationEvent
case class JoinedDoc(user: String) extends CollaborationEvent
case class LeftDoc(user: String) extends CollaborationEvent
case class SetDoc(doc: woot.WString) extends CollaborationEvent

sealed trait DocChange extends CollaborationEvent
// CursorChange user from to
// HightLight user from to
case class ChangeDoc(operation: woot.Operation) extends DocChange
case class ChangeBatchDoc(operations: List[woot.Operation]) extends DocChange
case object HeartBeat extends DocChange