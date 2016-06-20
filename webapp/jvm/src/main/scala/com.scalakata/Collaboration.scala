package com.scalakata

import akka.actor._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._

trait Collaboration {
  def flow(room: String, username: String): Flow[DocChange, CollaborationEvent, Any]
}

object Collaboration {
  def create(lobby: Lobby, system: ActorSystem): Collaboration = {
    val lobbyActor =
      system.actorOf(Props(new Actor {
        def receive: Receive = {
          case HeartBeat ⇒ ()
          case NewParticipant(room, username, subscriber) ⇒ {
            context.watch(subscriber)
            lobby.join(room, username, subscriber)
          }
          case ReceivedOperation(room, username, operation) ⇒ lobby.applyOps(room, username, List(operation))
          case ReceivedBatchOperation(room, username, operations) ⇒ lobby.applyOps(room, username, operations)
          case ParticipantLeft(room, username) ⇒ lobby.leave(room, username)
          case Terminated(sub) ⇒ lobby.leave(sub)
        }
      }))

    def collaborationInSink(room: String, username: String) =
      Sink.actorRef[CollabEvents2](lobbyActor, ParticipantLeft(room, username))

    new Collaboration {
      def flow(room: String, username: String): Flow[DocChange, CollaborationEvent, Any] = {
        val in = Flow[DocChange].map {
            case ChangeDoc(op) ⇒ ReceivedOperation(room, username, op)
            case ChangeBatchDoc(ops) ⇒ ReceivedBatchOperation(room, username, ops)
            case HeartBeat ⇒ HeartBeat2
          }.to(collaborationInSink(room, username))

        val out =
          Source.actorRef[CollaborationEvent](1, OverflowStrategy.fail)
            .mapMaterializedValue(lobbyActor ! NewParticipant(room, username, _))

        Flow.fromSinkAndSource(in, out)
      }
    }
  }
}

trait CollabEvents2
case class ParticipantLeft(room: String, username: String) extends CollabEvents2
case class NewParticipant(room: String, username: String, subscriber: ActorRef) extends CollabEvents2
case class ReceivedOperation(room: String, username: String, operation: woot.Operation) extends CollabEvents2
case class ReceivedBatchOperation(room: String, username: String, operations: List[woot.Operation]) extends CollabEvents2
case object HeartBeat2 extends CollabEvents2