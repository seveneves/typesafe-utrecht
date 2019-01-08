package typesafe.utrecht.typed
import akka.actor.testkit.typed.Effect.SpawnedAnonymous
import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import akka.actor.testkit.typed.scaladsl.TestInbox
import org.scalatest.FunSpecLike
import org.scalatest.Matchers

class ChatRoomSpec extends FunSpecLike with Matchers {

  describe("Typed Chat Room Sync") {
    it("should register a new session for a user") {
      val handlerInbox = TestInbox[String]()
      val sessionInbox = TestInbox[Session.SessionCommand]()
      val sessionReplyInbox = TestInbox[Chat.SessionReply]()
      val testKit =
        BehaviorTestKit(ChatRoom.behaviour()((_, _) => sessionInbox.ref))
      testKit.run(
        ChatRoom.RegisterUser("foo", handlerInbox.ref, sessionReplyInbox.ref)
      )
      sessionReplyInbox.expectMessage(Chat.SessionCreated(sessionInbox.ref))
      handlerInbox.expectMessage("Room: foo just joined chat room")
    }
  }

  describe("Typed Chat Room behavior") {
    it("should register a new session for a user") {
      val handlerInbox = TestInbox[String]()
      val sessionReplyInbox = TestInbox[Chat.SessionReply]()
      val testKit = BehaviorTestKit(ChatRoom.behaviour())
      testKit.run(
        ChatRoom.RegisterUser("foo", handlerInbox.ref, sessionReplyInbox.ref)
      )
      sessionReplyInbox.hasMessages shouldEqual true
      testKit.expectEffectType[SpawnedAnonymous[Session.SessionCommand]]
    }
  }

}
