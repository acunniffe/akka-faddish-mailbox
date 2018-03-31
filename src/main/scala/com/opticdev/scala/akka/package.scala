package com.opticdev.scala

package object akka {
  case class Ping(ballColor: String, speed: Int)
  case class Pong(speed: Int)
}
