package typesafe.utrecht.untyped

import akka.actor.ActorSystem
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import akka.testkit.TestProbe
import org.scalatest.FunSpecLike
import scala.concurrent.duration._

class ChatRoomSpec
    extends TestKit(ActorSystem("testing"))
    with FunSpecLike
    with ImplicitSender {
  private val sessionDummy = TestProbe()

  describe("Chat Room Actor") {
    it("should register a new session for a user") {
      val handler = TestProbe()
      val chatRoom = system.actorOf(ChatRoom.props((_, _) => sessionDummy.ref))
      chatRoom ! ChatRoom.RegisterUser("foo", handler.ref)
      expectMsg(Chat.SessionCreated(sessionDummy.ref))
      handler.expectMsg("Room: foo just joined chat room")
    }

    it("should remove user from chat room") {
      val handler = TestProbe()
      val chatRoom = system.actorOf(ChatRoom.props((_, _) => sessionDummy.ref))
      chatRoom ! ChatRoom.RegisterUser("foo", handler.ref)
      expectMsgType[Chat.SessionCreated]
      chatRoom ! ChatRoom.UserClosedRoom("foo")
      chatRoom ! ChatRoom.SendMessage("bar", "other message")
      handler.expectMsg("Room: foo just joined chat room")
      handler.expectNoMessage(100.millis)
    }

    it("should close chat room by termination") {
      val chatRoom = system.actorOf(ChatRoom.props())
      watch(chatRoom)
      chatRoom ! ChatRoom.Close
      expectTerminated(chatRoom)
    }

    it("should send message to empty handlers") {
      val handler = TestProbe()
      val chatRoom = system.actorOf(ChatRoom.props((_, _) => sessionDummy.ref))
      chatRoom ! ChatRoom.RegisterUser("foo", handler.ref)
      expectMsgType[Chat.SessionCreated]
      chatRoom ! ChatRoom.SendMessage("foo", "message")
      handler.fishForMessage() {
        case "foo: message" => true
        case _ => false
      }
    }
  }
}
