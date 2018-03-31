package com.opticdev.scala.akka

import akka.dispatch.Envelope

class TestFilter extends FaddishMailboxFilter {
  override def filterOut(target: Envelope) : PartialFunction[Any, Boolean] = {
    target.message match {
      case ping: Ping =>
        val newBallColor = ping.ballColor
        PartialFunction[Any, Boolean] {
          case ping: Ping => ping.ballColor == newBallColor
          case _ => false
        }
      case _ =>
        super.filterOut(target)
    }
  }
}
