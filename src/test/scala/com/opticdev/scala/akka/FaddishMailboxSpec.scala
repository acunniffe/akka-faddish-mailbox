package com.opticdev.scala.akka

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FunSpecLike, Matchers, WordSpecLike}

class FaddishMailboxSpec extends TestKit(ActorSystem("FaddishMailboxSpec")) with ImplicitSender with FunSpecLike with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  it("will filter out messages") {
    //uses TestActor and TestFaddishFilter
    val echo = system.actorOf(Props[TestActor].withDispatcher("my-mailbox"))
    echo ! Ping("RED", 20)
    (2 to 40).foreach(i=> echo ! Ping("RED", 20 * i)) //invalidated, never processed
    echo ! Ping("RED", 0)

    expectMsg(Pong(20))
    expectMsg(Pong(0))
  }

  it("if no filter is specified none will be applied ") {
    //uses TestActor and TestFaddishFilter
    val echo = system.actorOf(Props[TestActor].withDispatcher("other-mailbox"))
    echo ! Ping("RED", 20)
    echo ! Ping("RED", 40)
    echo ! Ping("RED", 60)
    echo ! Ping("RED", 0)

    expectMsg(Pong(20))
    expectMsg(Pong(40))
    expectMsg(Pong(60))
    expectMsg(Pong(0))
  }

}
