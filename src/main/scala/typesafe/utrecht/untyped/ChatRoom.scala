package typesafe.utrecht.untyped

import akka.actor.Actor
import akka.actor.ActorContext
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props

class ChatRoom(sessionCreator: (String, ActorContext) => ActorRef)
    extends Actor
    with ActorLogging {

  override def receive: Receive = onMessage()

  private def onMessage(
    handlers: Map[String, ActorRef] = Map.empty[String, ActorRef]
  ): Receive = {
    case ChatRoom.RegisterUser(name, handler) =>
      context.become(onMessage(handlers + (name -> handler)))
      self ! ChatRoom.SendMessage("Room", s"$name just joined chat room")
      sender() ! Chat.SessionCreated(sessionCreator(name, context))
    case ChatRoom.SendMessage(user, message) =>
      handlers.values.foreach(_ ! s"$user: $message")
    case ChatRoom.Close =>
      handlers.values.foreach(_ ! "The room is closed")
      context.stop(self)
    case ChatRoom.UserClosedRoom(username) =>
      log.info(s"User $username closed chat room")
      context.become(onMessage(handlers - username))
      self ! ChatRoom.SendMessage("Room", s"User $username left room")
  }
}

object ChatRoom {

  def props(): Props = {
    props((name, ctx) => ctx.actorOf(Session.props(name, ctx.self)))
  }

  def props(sessionCreator: (String, ActorContext) => ActorRef): Props = {
    Props(new ChatRoom(sessionCreator))
  }

  case class RegisterUser(userName: String, handler: ActorRef)

  case object Close

  case class SendMessage(userName: String, message: String)

  case class UserClosedRoom(username: String)

}
