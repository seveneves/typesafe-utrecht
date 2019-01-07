package typesafe.utrecht.untyped

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout

import concurrent.duration._
import scala.concurrent.Future

object Main extends App {

  implicit private val timeout: Timeout = 1.second

  private val actorSystem = ActorSystem("ChatDemoApp")
  import actorSystem.dispatcher
  private val chat = actorSystem.actorOf(Chat.props())
  private val roomName = "Typesafe Utrecht"

  chat ! Chat.CreateRoom(roomName)

  val mikesSession: Future[Chat.SessionCreated] = (
    chat ? Chat.JoinRoom(
      roomName,
      "Mike",
      actorSystem.actorOf(ConsoleMessageHandler.props("Mike", roomName)))
  ).mapTo[Chat.SessionCreated]

  chat ! Chat.JoinRoom(
    roomName,
    "John",
    actorSystem.actorOf(ConsoleMessageHandler.props("John", roomName))
  )

  mikesSession.foreach { session =>
    session.session ! Session.SendMessage("Welcome to Typesafe Utrecht")
    session.session ! Session.SendMessage(
      "Our next topic is how to save the world with type safety"
    )
    session.session ! Session.Close
  }

  Thread.sleep(1000)
  chat ! Chat.CloseRoom(roomName)
  actorSystem.terminate()
}
