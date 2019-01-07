package typesafe.utrecht.untyped

import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.testkit.TestProbe
import org.scalatest.FunSpecLike

class SessionSpec extends TestKit(ActorSystem("testing")) with FunSpecLike {

  describe("Session Actor") {
    it("should send message to chat room actor") {
      val charRoomProbe = TestProbe()
      val session = system.actorOf(Session.props("foo", charRoomProbe.ref))
      session ! Session.SendMessage("Hello there")
      charRoomProbe.expectMsg(ChatRoom.SendMessage("foo", "Hello there"))
    }

    it("should send close command to chat room actor") {
      val charRoomProbe = TestProbe()
      val session = system.actorOf(Session.props("foo", charRoomProbe.ref))
      session ! Session.Close
      charRoomProbe.expectMsg(ChatRoom.UserClosedRoom("foo"))
    }
  }
}
