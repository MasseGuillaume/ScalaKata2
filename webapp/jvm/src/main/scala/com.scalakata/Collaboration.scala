package com.scalakata

import akka.actor._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import akka.stream.ActorMaterializer

trait Collaboration {
  def flow(room: String, username: String): Flow[DocChange, CollaborationEvent, Any]
}

class Loby() {
  type Room = Map[String, ActorRef]
  private var rooms = Map.empty[String, (woot.WString, Room)]

  def update(room: String, f: Room ⇒ Room): Unit = {
    rooms =
      rooms.get(room) match {
        case Some((doc, r)) ⇒ rooms.updated(room, (doc, f(r)))
        case None ⇒ rooms
      }
  }

  def join(room: String, username: String, ref: ActorRef): Unit = {
    val (doc, r) =
      rooms.get(room) match {
        case Some((doc, r)) ⇒ {
          // user join existing room
          (doc, r + (username -> ref))
        }
        case None ⇒ {
          // room created
          val (_, fullDoc) = woot.WString.empty().insert(Util.wrap(""))
          (fullDoc, Map(username -> ref))
        }
      }

    rooms = rooms.updated(room, (doc, r))
    ref ! SetDoc(doc.copy(site = woot.SiteId.random))

    broadcast(room, JoinedDoc(username), Some(username))
  }

  def applyOps(room: String, username: String, ops: List[woot.Operation]): Unit = {
    rooms =
      rooms.get(room) match {
        case Some((doc, r)) ⇒ {
          val updatedDoc = 
            ops.foldLeft(doc){ case (d, op) ⇒
              d.integrate(op)._2
            }
          broadcast(room, ChangeBatchDoc(ops), Some(username))
          rooms.updated(room, (updatedDoc, r))
        }
        case None ⇒ rooms
      }
  }

  // connection reset
  def leave(sub: ActorRef): Unit = {
    rooms = rooms.map{ case (room, (doc, users)) ⇒
      room -> ((doc, users.filterNot{ case (username, s) ⇒
        if(s == sub) broadcast(room, LeftDoc(username), Some(username))
        s == sub
      }))
    }
  }

  def leave(room: String, username: String): Unit = {
    rooms =
      rooms.get(room) match {
        case Some((doc, r)) ⇒ r.get(username) match {
          case Some(ref) ⇒ {
            ref ! Status.Success(Unit)
            val newRoom = r - username
            if(newRoom.size == 0) rooms - room // empty room
            else rooms.updated(room, (doc, newRoom))
          }
          case None ⇒ rooms // user not found
        }
        case None ⇒ rooms // room not found
      }
    broadcast(room, LeftDoc(username), Some(username))
  }
  private def broadcast(room: String, event: CollaborationEvent, username: Option[String] = None): Unit =
    rooms.get(room).foreach{ 
      case (_, users) ⇒
        val filteredUsers =
          username match {
            case Some(userToRemove) ⇒ users.filterKeys(_ != userToRemove)
            case None ⇒ users
          }
        filteredUsers.values.foreach( _ ! event)
    }
}

object Collaboration {
  def create(system: ActorSystem): Collaboration = {
    val lobbyActor =
      system.actorOf(Props(new Actor {
        val loby = new Loby()
        def receive: Receive = {
          case HeartBeat ⇒ ()
          case NewParticipant(room, username, subscriber) ⇒ {
            context.watch(subscriber)
            loby.join(room, username, subscriber)
          }
          case ReceivedOperation(room, username, operation) ⇒ loby.applyOps(room, username, List(operation))
          case ReceivedBatchOperation(room, username, operations) ⇒ loby.applyOps(room, username, operations)
          case ParticipantLeft(room, username) ⇒ loby.leave(room, username)
          case Terminated(sub) ⇒ loby.leave(sub)
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