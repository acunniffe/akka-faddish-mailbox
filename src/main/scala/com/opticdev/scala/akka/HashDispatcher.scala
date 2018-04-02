package com.opticdev.scala.akka

import java.math.BigInteger

import scala.collection.immutable

class HashDispatcher[W](workers: immutable.IndexedSeq[W]) {
  require(workers.nonEmpty, "HashDispatchers requires at least one worker.")
  def workerForHash(hash: String) = HashDispatcher.workerForHash(hash, workers)
  def workerForHash(hash: BigInteger) = HashDispatcher.workerForHash(hash, workers)
}

object HashDispatcher {
  def workerForHash[W](hash: String, workers: immutable.IndexedSeq[W]) = {
    val bigInt = new BigInteger(hash, 16)
    workers(bigInt.mod(BigInteger.valueOf(workers.size)).intValue())
  }

  def workerForHash[W](hash: BigInteger, workers: immutable.IndexedSeq[W]) = {
    workers(hash.mod(BigInteger.valueOf(workers.size)).intValue())
  }

  def sha256Hash(text: String) : BigInteger = new java.math.BigInteger(1, java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8")))

}
