package typesafe.utrecht.typed
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Session {

  def behaviour(
    username: String,
    chatRoom: ActorRef[ChatRoom.ChatRoomCommand]
  ): Behavior[SessionCommand] = {
    Behaviors.receive[SessionCommand] {
      case (_, Session.SendMessage(message)) =>
        chatRoom.tell(ChatRoom.SendMessage(username, message))
        Behavior.same
      case (_, Session.Close) =>
        chatRoom.tell(ChatRoom.UserClosedRoom(username))
        Behavior.same
    }
  }

  sealed trait SessionCommand

  case object Close extends SessionCommand

  case class SendMessage(message: String) extends SessionCommand
}
