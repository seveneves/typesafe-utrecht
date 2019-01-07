package typesafe.utrecht.untyped

import akka.actor.{Actor, ActorRef, Props}

class ChatRoom extends Actor {
  private var handlers = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case ChatRoom.RegisterUser(name, handler) =>
      handlers += name -> handler
      self ! ChatRoom.SendMessage("Room", s"$name just joined chat room")
      sender() ! Chat.SessionCreated(context.actorOf(Session.props(name, self)))
    case ChatRoom.SendMessage(user, message) =>
      handlers.values.foreach(_ ! s"$user: $message")
    case ChatRoom.Close =>
      handlers.values.foreach(_ ! "The room is closed")
      context.stop(self)
  }
}

object ChatRoom {
  def props() = Props(new ChatRoom())

  case class RegisterUser(userName: String, handler: ActorRef)

  case object Close

  case class SendMessage(userName: String, message: String)

  case class UserClosedRoom(username: String)

}
