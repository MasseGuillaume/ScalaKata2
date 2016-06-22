package com.scalakata

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}

trait RoomTracking {
  def flow(username: String): Flow[KeepAlive, RoomListEvent, Any]
}

object RoomTracking {

  def create(lobby: Lobby, system: ActorSystem): RoomTracking = {
    val clientTrackingActor = system.actorOf(Props(new Actor {
      var clients = Map.empty[String, ActorRef]

      def receive: Receive = {
        case NewClient(username, ref) => clients = clients.updated(username, ref); ref ! SetRooms(lobby.activeRooms)
        case ClientExit(username)     => clients = clients - username
        case event: RoomListEvent     => clients.values.foreach(_ ! event)
        case KeepAlive                => // do nothing
      }
    }))

    system.eventStream.subscribe(clientTrackingActor, classOf[RoomListEvent])

    new RoomTracking {
      def flow(username: String): Flow[KeepAlive, RoomListEvent, Any] = {
        val in = Flow[KeepAlive].to(Sink.actorRef(clientTrackingActor, ClientExit(username)))

        val out = Source.actorRef[RoomListEvent](1, OverflowStrategy.fail)
          .mapMaterializedValue[Unit](client => clientTrackingActor ! NewClient(username, client))

        Flow.fromSinkAndSource(in, out)
      }
    }
  }

  sealed trait Events
  case class NewClient(username: String, ref: ActorRef) extends Events
  case class ClientExit(username: String) extends Events

}
