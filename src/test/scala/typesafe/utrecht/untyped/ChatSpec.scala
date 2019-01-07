package typesafe.utrecht.untyped

import akka.actor.ActorSystem
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import akka.testkit.TestProbe
import org.scalatest.FunSpecLike

class ChatSpec
    extends TestKit(ActorSystem())
    with FunSpecLike
    with ImplicitSender {

  describe("Chat Actor") {
    it("should not allow joining if room does not exist") {
      val dummyRoom = TestProbe()
      val chat = system.actorOf(Chat.props(_ => dummyRoom.ref))
      chat ! Chat.JoinRoom("non-existing", "foo", TestProbe().ref)
      expectMsg(Chat.RoomDoesNotExist)
    }

    it("should allow joining if room does not exist") {
      val dummyRoom = TestProbe()
      val handler = TestProbe()
      val chat = system.actorOf(Chat.props(_ => dummyRoom.ref))
      chat ! Chat.CreateRoom("existing")
      chat ! Chat.JoinRoom("existing", "foo", handler.ref)
      dummyRoom.expectMsg(ChatRoom.RegisterUser("foo", handler.ref))
    }

    it("should send close command to chat room ") {
      val dummyRoom = TestProbe()
      val chat = system.actorOf(Chat.props(_ => dummyRoom.ref))
      chat ! Chat.CreateRoom("room-to-close")
      chat ! Chat.CloseRoom("room-to-close")
      dummyRoom.expectMsg(ChatRoom.Close)
    }
  }

}
