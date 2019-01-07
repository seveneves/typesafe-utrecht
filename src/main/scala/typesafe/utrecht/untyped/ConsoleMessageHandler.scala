package typesafe.utrecht.untyped

import akka.actor.Actor
import akka.actor.Props

class ConsoleMessageHandler(username: String, chatRoom: String) extends Actor {
  override def receive: Receive = {
    case message => println(s"$chatRoom: $username received $message")
  }
}

object ConsoleMessageHandler {

  def props(username: String, chatRoom: String) =
    Props(new ConsoleMessageHandler(username, chatRoom))
}
