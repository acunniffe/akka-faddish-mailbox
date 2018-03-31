name := "akka-faddish-mailbox"
organization := "com.opticdev"
version := "0.1.0"

scalaVersion := "2.12.4"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.4"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.4" % "test"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"