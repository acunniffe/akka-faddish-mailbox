package com.opticdev.scala.akka

import scala.collection.immutable
import java.util.concurrent.ThreadLocalRandom

import akka.routing._

import scala.util.{Random, Try}

case class HashDispatchedRoutingLogic(getMessageKey: (Any) => Option[String]) extends RoutingLogic {
  def select(message: Any, routees: immutable.IndexedSeq[Routee]): Routee = {
    if (routees.isEmpty) return NoRoutee

    val keyOption = Try { getMessageKey(message) }.toOption.flatten

    keyOption.collect {
      case key: String => {
        import HashDispatcher._
        workerForHash(sha256Hash(key), routees)
      }
    }.getOrElse(routees(Random.nextInt(routees.size))) //fallback to random

  }
}