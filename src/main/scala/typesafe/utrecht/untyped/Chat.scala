package typesafe.utrecht.untyped

import akka.actor.Actor
import akka.actor.ActorContext
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated

class Chat(chatRoomCreator: ActorContext => ActorRef)
    extends Actor
    with ActorLogging {

  private var rooms = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case Chat.CreateRoom(name) if rooms.keySet.contains(name) =>
      log.warning(s"Room $name already exists")

    case Chat.CreateRoom(name) =>
      log.info(s"Creating new room $name")
      val chatRoomRef = chatRoomCreator(context)
      rooms += name -> chatRoomRef
      context.watch(chatRoomRef)

    case Chat.JoinRoom(name, userName, handler)
        if rooms.keySet.contains(name) =>
      log.info(s"User $userName is joining room $name")
      rooms(name).forward(ChatRoom.RegisterUser(userName, handler))

    case Chat.JoinRoom(name, _, _) =>
      log.warning(s"Room does not exists $name")
      sender() ! Chat.RoomDoesNotExist

    case Terminated(charRoom) =>
      rooms.find(_._2 == charRoom).foreach {
        case (name, _) =>
          log.info(s"Room $name was closed")
          rooms -= name
      }

    case Chat.CloseRoom(name) if rooms.keySet.contains(name) =>
      rooms(name) ! ChatRoom.Close
  }
}

object Chat {

  def props(chatRoomCreator: ActorContext => ActorRef): Props = {
    Props(new Chat(chatRoomCreator))
  }

  def props(): Props = props(_.actorOf(ChatRoom.props()))

  case class CreateRoom(name: String)

  case class CloseRoom(name: String)

  case object RoomDoesNotExist

  case class JoinRoom(
    roomName: String,
    userName: String,
    messageHandler: ActorRef
  )

  case class SessionCreated(session: ActorRef)
}
