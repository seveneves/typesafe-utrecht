package typesafe.utrecht.typed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorContext
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import typesafe.utrecht.typed.Session.SessionCommand

object ChatRoom {

  def behaviour(handlers: Map[String, ActorRef[String]] = Map.empty)(
    implicit sessionCreator: (
      String,
      ActorContext[ChatRoomCommand]) => ActorRef[SessionCommand] =
      (username, ctx) =>
        ctx.asScala.spawnAnonymous(
          Session.behaviour(username, ctx.asScala.self)))
    : Behavior[ChatRoomCommand] = {
    Behaviors.receive[ChatRoomCommand] {
      case (ctx, ChatRoom.RegisterUser(name, handler, sender)) =>
        ctx.self ! ChatRoom.SendMessage("Room", s"$name just joined chat room")
        sender ! Chat.SessionCreated(sessionCreator(name, ctx))
        behaviour(handlers + (name -> handler))
      case (_, ChatRoom.SendMessage(user, message)) =>
        handlers.values.foreach(_ ! s"$user: $message")
        Behavior.same[ChatRoomCommand]
      case (_, ChatRoom.Close) =>
        handlers.values.foreach(_ ! "The room is closed")
        Behavior.stopped
      case (ctx, ChatRoom.UserClosedRoom(username)) =>
        ctx.log.info(s"User $username closed chat room")
        ctx.self ! ChatRoom.SendMessage("Room", s"User $username left room")
        behaviour(handlers - username)
    }
  }

  sealed trait ChatRoomCommand

  case class RegisterUser(userName: String,
                          handler: ActorRef[String],
                          sender: ActorRef[Chat.SessionCreated])
      extends ChatRoomCommand

  case object Close extends ChatRoomCommand

  case class SendMessage(userName: String, message: String)
      extends ChatRoomCommand

  case class UserClosedRoom(username: String) extends ChatRoomCommand

}
