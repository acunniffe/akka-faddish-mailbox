package com.opticdev.scala.akka

import org.scalatest.FunSpec

class HashDispatcherSpec extends FunSpec {

  def sha256Hash(text: String) : String = String.format("%064x", new java.math.BigInteger(1, java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8"))))

  it("Distributes tasks quasi randomly among workers") {
    val hashDispatcher = new HashDispatcher(Vector("A", "B", "C", "D", "E", "F"))
    val groups = (0 to 50000).map(i=> hashDispatcher.workerForHash(sha256Hash(i.toString))).groupBy(i=> i)
    assert(groups.map(_._2.size) == List(8377, 8333, 8412, 8266, 8210, 8403))
  }

  it("Only one worker gets all tasks") {
    val hashDispatcher = new HashDispatcher(Vector("A"))
    val groups = (0 until 500).map(i=> hashDispatcher.workerForHash(sha256Hash(i.toString))).groupBy(i=> i)
    assert(groups.map(_._2.size) == List(500))
  }

  it("throws if no workers") {
    assertThrows[Exception] {
      new HashDispatcher[String](Vector())
    }
  }

}
