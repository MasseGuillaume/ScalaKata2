package com.scalakata

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.ReactTagOf
import japgolly.scalajs.react.vdom.all._
import org.scalajs.dom._
import org.scalajs.dom.raw.HTMLElement
import upickle.default.{read => uread, write => uwrite}

import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

@JSExport
object RoomList {

  @JSExport
  def init(elemId: String): Unit = {
    val protocol = if (window.location.protocol == "https:") "wss" else "ws"
    val username = java.util.UUID.randomUUID().toString
    val uri = s"$protocol://${window.location.host}/room-list?username=$username"

    case class State(ws: Option[WebSocket], rooms: Map[String, Vector[String]])

    class Backend($: BackendScope[Unit, State]) {

      def start: Callback = {

        def connect = CallbackTo[WebSocket] {

          // Get direct access so WebSockets API can modify state directly
          // (for access outside of a normal DOM/React callback).
          val direct = $.accessDirect

          def onOpen(e: Event): Unit = {
            direct.state.ws.foreach(ws => window.setInterval(() => ws.send(uwrite(KeepAlive())), 10000))
          }

          def onMessage(e: MessageEvent): Unit = {
            uread[RoomListEvent](e.data.toString) match {
              case NewRoom(roomName, user)     => direct.modState(s => s.copy(rooms = s.rooms + (roomName -> Vector(user))))
              case CloseRoom(roomName)         => direct.modState(s => s.copy(rooms = s.rooms - roomName))
              case UpdateRoom(roomName, users) => direct.modState(s => s.copy(rooms = s.rooms.updated(roomName, users)))
              case SetRooms(rooms)             => direct.modState(_.copy(rooms = rooms))
            }
          }

          def onerror(e: ErrorEvent): Unit = console.log(s"Error: ${e.message}")

          def onClose(e: CloseEvent): Unit = direct.modState(_.copy(ws = None))

          // Create WebSocket and setup listeners
          val ws = new WebSocket(uri)
          ws.onopen = onOpen _
          ws.onclose = onClose _
          ws.onmessage = onMessage _
          ws.onerror = onerror _
          ws
        }

        connect.attemptTry.flatMap {
          case Success(ws)    => $.modState(_.copy(ws = Some(ws)))
          case Failure(error) => console.log(error.toString); Callback.empty
        }
      }

      def end: Callback = {
        def closeWebSocket = $.state.map(_.ws.foreach(_.close()))
        def clearWebSocket = $.modState(_.copy(ws = None))
        closeWebSocket >> clearWebSocket
      }

      def createRoom(e: ReactEventI) = {
        val name = window.prompt("Room name", "")
        if(name != "") window.location.replace(s"/room/$name")
        Callback.log(s"Redirect to room $name")
      }

      def render(state: State): ReactTagOf[HTMLElement] = {
        ul(cls := "drawer-menu",
          li(cls := "drawer-brand",
            i(id := "close-drawer-btn", cls := "oi", "data-glyph".reactAttr := "chevron-right"),
            span(s"Rooms (${state.rooms.size})")
          ),

          li(cls := "create-room", onClick ==> createRoom)(
            i(cls := "oi", "data-glyph".reactAttr := "plus"),
            span("Create room")
          ),

          state.rooms.map { case (roomName, users) =>
            li(cls := "drawer-menu-item",
              a(href := s"/room/$roomName",
                h3(cls := "drawer-menu-item-title", roomName),
                h5(cls := "drawer-menu-item-subtitle", s"${users.size} Participants")
            ))
          }
        )
      }
    }

    val roomList = ReactComponentB[Unit]("roomList")
      .initialState(State(None, Map.empty))
      .renderBackend[Backend]
      .componentDidMount(_.backend.start)
      .componentWillUnmount(_.backend.end)
      .build

    ReactDOM.render(roomList(), document.getElementById(elemId))
    ()
  }
}
