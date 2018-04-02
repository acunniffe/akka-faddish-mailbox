package com.opticdev.scala.akka

import java.math.BigInteger

class HashDispatcher[W](workers: Vector[W]) {
  require(workers.nonEmpty, "HashDispatchers requires at least one worker.")
  private val numberOfContainers = new BigInteger(workers.size.toString)

  def workerForHash(hash: String) = {
    import java.math.BigInteger
    val bigInt = new BigInteger(hash, 16)
    workers(bigInt.mod(numberOfContainers).intValue())
  }

}
