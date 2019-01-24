package typesafe.utrecht.typed
import akka.actor.typed.ActorSystem

object Runner extends App {

  val behavior = Main.behavior()
  val system = ActorSystem[Unit](behavior, "typed-runner")


}
