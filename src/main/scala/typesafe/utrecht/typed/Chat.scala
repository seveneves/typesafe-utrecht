package typesafe.utrecht.typed

import akka.actor.typed.ActorContext
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed
import typesafe.utrecht.typed.ChatRoom.ChatRoomCommand
import typesafe.utrecht.typed.Session.SessionCommand

object Chat {

  def behaviour(
    rooms: Map[String, typed.ActorRef[ChatRoomCommand]] = Map.empty)(
    implicit chatRoomCreator: ActorContext[_] => ActorRef[ChatRoomCommand] =
      ctx => ctx.asScala.spawnAnonymous(ChatRoom.behaviour())): Behavior[ChatCommand] = {
    Behaviors.receive[ChatCommand] {
      case (ctx, Chat.CreateRoom(name)) if rooms.keySet.contains(name) =>
        ctx.log.warning(s"Room $name already exists")
        Behavior.same[ChatCommand]
      case (ctx, Chat.CreateRoom(name)) =>
        ctx.log.info(s"Creating new room $name")
        val chatRoomRef = chatRoomCreator(ctx)
        ctx.watchWith(chatRoomRef, RoomClosed(name))
        behaviour(rooms + (name -> chatRoomRef))
      case (ctx, Chat.JoinRoom(name, userName, handler, sender))
          if rooms.keySet.contains(name) =>
        ctx.log.info(s"User $userName is joining room $name")
        rooms(name).tell(ChatRoom.RegisterUser(userName, handler, sender))
        Behavior.same[ChatCommand]
      case (ctx, Chat.JoinRoom(name, _, _, sender)) =>
        ctx.log.warning(s"Room does not exists $name")
        sender.tell(RoomDoesNotExist)
        Behavior.same[ChatCommand]
      case (ctx, RoomClosed(chatRoom)) =>
        ctx.log.info(s"Room $chatRoom is closed")
        behaviour(rooms.filterKeys(!_.equals(chatRoom)))
      case (ctx, Chat.CloseRoom(name)) if rooms.keySet.contains(name) =>
        ctx.log.info(s"Closing $name room")
        rooms(name).tell(ChatRoom.Close)
        Behavior.same[ChatCommand]
    }
  }

  sealed trait ChatCommand

  case class CreateRoom(name: String) extends ChatCommand

  case class CloseRoom(name: String) extends ChatCommand

  case class RoomClosed(name: String) extends ChatCommand

  case class JoinRoom(
    roomName: String,
    userName: String,
    messageHandler: ActorRef[String],
    sender: ActorRef[SessionReply]
  ) extends ChatCommand

  sealed trait SessionReply

  case object RoomDoesNotExist extends SessionReply

  case class SessionCreated(session: ActorRef[SessionCommand])
      extends SessionReply
}
