package typesafe.utrecht.untyped

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

class Session(userName: String, chatRoom: ActorRef) extends Actor {
  override def receive: Receive = {
    case Session.SendMessage(message) =>
      chatRoom ! ChatRoom.SendMessage(userName, message)
    case Session.Close =>
      chatRoom ! ChatRoom.UserClosedRoom(userName)
  }
}

object Session {

  def props(username: String, charRoom: ActorRef) = {
    Props(new Session(username, charRoom))
  }

  case object Close

  case class SendMessage(message: String)
}
