package com.opticdev.scala.akka

import akka.actor.ActorRef
import akka.routing.Routee
import org.scalatest.FunSpec

class HashDispatchedRoutingLogicSpec extends FunSpec {

  case class ParseJob(filePath: String, contents: String)
  case class MockRoutee(id: String) extends Routee {
    override def send(message: Any, sender: ActorRef): Unit = ???
  }

  val routees = scala.collection.immutable.IndexedSeq(MockRoutee("GREEN"), MockRoutee("RED"), MockRoutee("BLUE"), MockRoutee("WHITE"), MockRoutee("BLACK"))

  lazy val testLogic = HashDispatchedRoutingLogic({
    case a: ParseJob => Some(a.filePath)
    case _ => None
  })

  it("can instantiate HashDispatched logic") {
    testLogic
  }

  it("will select the same route each time for each job key") {
    val jobs = Seq(
      ParseJob("a/b/c", "testJob"),
      ParseJob("a/b/c", "testJob1"),
      ParseJob("a/b/c", "testJob2"),
      ParseJob("a/b/c", "testJob3")
    )

    assert(jobs.map(j=> testLogic.select(j, routees)).distinct == Seq(MockRoutee("BLUE")))
  }

  it ("will return a random route if message isn't recodnized") {
    assert(Array.fill(50)("NOT VALID").map(s=> testLogic.select(s, routees)).distinct.size > 1)
  }

}
