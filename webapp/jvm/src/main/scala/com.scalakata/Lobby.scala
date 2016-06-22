package com.scalakata

import akka.actor._

class Lobby(system: ActorSystem) {
  type RoomName = String
  type Username = String
  type Room = Map[Username, ActorRef]
  private var rooms = Map.empty[RoomName, (woot.WString, Room)]

  def activeRooms: Map[RoomName, Vector[Username]] = rooms.map(room => (room._1, room._2._2.keys.toVector))

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
          val updatedRoom = r + (username -> ref)
          system.eventStream.publish(UpdateRoom(room, updatedRoom.keys.toVector))
          (doc, updatedRoom)
        }
        case None ⇒ {
          // room created
          system.eventStream.publish(NewRoom(room, username))
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
      val (disconnectUsers, connectUsers) = users.partition { case (_, s) => s == sub }
      disconnectUsers.foreach { case (username, _) => broadcast(room, LeftDoc(username), Some(username)) }
      room -> ((doc, connectUsers))
    }
  }

  def leave(room: String, username: String): Unit = {
    rooms =
      rooms.get(room) match {
        case Some((doc, r)) ⇒ r.get(username) match {
          case Some(ref) ⇒ {
            ref ! Status.Success(Unit)
            val newRoom = r - username
            if (newRoom.size == 0) { // empty room
              system.eventStream.publish(CloseRoom(room))
              rooms - room
            } else {
              system.eventStream.publish(UpdateRoom(room, newRoom.keys.toVector))
              rooms.updated(room, (doc, newRoom))
            }
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
