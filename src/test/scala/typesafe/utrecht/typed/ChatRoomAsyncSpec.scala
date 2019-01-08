package typesafe.utrecht.typed

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.testkit.typed.scaladsl.TestProbe
import org.scalatest.FunSpecLike
import org.scalatest.Matchers

class ChatRoomAsyncSpec
    extends ScalaTestWithActorTestKit
    with FunSpecLike
    with Matchers {

  describe("Typed Chat Room") {
    it("should register a new session for a user") {
      val handlerProbe = TestProbe[String]()
      val sessionProbe = TestProbe[Session.SessionCommand]()
      val sessionReplyProbe = TestProbe[Chat.SessionReply]()
      val charRoom = spawn(ChatRoom.behaviour()((_, _) => sessionProbe.ref))
      charRoom ! ChatRoom.RegisterUser("foo",
                                       handlerProbe.ref,
                                       sessionReplyProbe.ref)
      sessionReplyProbe.expectMessage(Chat.SessionCreated(sessionProbe.ref))
      handlerProbe.expectMessage("Room: foo just joined chat room")
    }
  }
}
