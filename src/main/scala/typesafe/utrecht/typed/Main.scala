package typesafe.utrecht.typed

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.DispatcherSelector
import akka.util.Timeout
import typesafe.utrecht.typed.Chat.RoomDoesNotExist
import typesafe.utrecht.typed.Chat.SessionCreated
import typesafe.utrecht.untyped.ConsoleMessageHandler

import scala.concurrent.duration._
import scala.util.Success

object Main {

  implicit private val timeout: Timeout = 1.second
  private val roomName = "Typesafe Utrecht"

  def behavior(): Behavior[Unit] = {
    Behaviors.setup[Unit] { ctx =>
      implicit val scheduler = ctx.system.scheduler
      implicit val executionContext =
        ctx.system.dispatchers.lookup(DispatcherSelector.default())

      val chat = ctx.spawn(Chat.behaviour(), "chat")
      val consoleHandler = ctx.toUntyped
        .actorOf(ConsoleMessageHandler.props("Mike", roomName))
      val consoleHandlerJohn = ctx.toUntyped
        .actorOf(ConsoleMessageHandler.props("John", roomName))

      val ignore: ActorRef[Chat.SessionReply] =
        ctx.messageAdapter[Chat.SessionReply](_ => ())

      chat ! Chat.CreateRoom(roomName)
      chat ! Chat.JoinRoom(roomName,
                           "John",
                           consoleHandlerJohn.toTyped[String],
                           ignore)
      (chat ? { replyTo: ActorRef[Chat.SessionReply] =>
        Chat.JoinRoom(roomName, "Mike", consoleHandler.toTyped[String], replyTo)
      }).onComplete {
        case Success(RoomDoesNotExist) =>
          println("Room is not created")
        case Success(SessionCreated(session)) =>
          session ! Session.SendMessage("Welcome to Typesafe Utrecht")
          session ! Session.SendMessage(
            "Our next topic is how to save the world with type safety"
          )
          session ! Session.Close
      }
      Thread.sleep(1000)
      chat ! Chat.CloseRoom(roomName)
      Behavior.stopped
    }
  }
}
