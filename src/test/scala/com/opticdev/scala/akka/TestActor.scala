package com.opticdev.scala.akka

import akka.actor.Actor
import akka.dispatch.RequiresMessageQueue

class TestActor extends Actor with RequiresMessageQueue[FaddishUnboundedMessageQueueSemantics] {
  override def receive: Receive = {
    case Ping(ballColor, rating) => {
      Thread.sleep(1000)
      sender() ! Pong(rating)
    }
  }
}