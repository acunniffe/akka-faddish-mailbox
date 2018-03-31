package com.opticdev.scala.akka

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.dispatch.Envelope
import akka.dispatch.MailboxType
import akka.dispatch.MessageQueue
import akka.dispatch.ProducesMessageQueue
import com.typesafe.config.Config
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer

import scala.util.Try

trait FaddishUnboundedMessageQueueSemantics

abstract class FaddishMailboxFilter {
  def filterOut(target: Envelope) : PartialFunction[Any, Boolean] = {
    PartialFunction[Any, Boolean] {
      case _ => false
    }
  }
}

object FaddishUnboundedMailbox {

  class FaddishMessageQueue(filter: FaddishMailboxFilter) extends MessageQueue
    with FaddishUnboundedMessageQueueSemantics {

    private final val queue = new ConcurrentLinkedQueue[Envelope]()

    def enqueue(receiver: ActorRef, handle: Envelope): Unit = {

      synchronized {
        val toRemove = scala.collection.mutable.Set[Envelope]()
        queue.iterator().forEachRemaining(new Consumer[Envelope] {
          override def accept(t: Envelope): Unit = {
            if (filter.filterOut(handle)(t.message)) {
              toRemove += t
            }
          }
        })

        toRemove.foreach(i=> queue.remove(i))
      }

      queue.offer(handle)
    }
    def dequeue(): Envelope = queue.poll()
    def numberOfMessages: Int = queue.size
    def hasMessages: Boolean = !queue.isEmpty
    def cleanUp(owner: ActorRef, deadLetters: MessageQueue) {
      while (hasMessages) {
        deadLetters.enqueue(owner, dequeue())
      }
    }
  }
}

// This is the Mailbox implementation
class FaddishUnboundedMailbox(filter: FaddishMailboxFilter) extends MailboxType
  with ProducesMessageQueue[FaddishUnboundedMailbox.FaddishMessageQueue] {

  import FaddishUnboundedMailbox._

  // This constructor signature must exist, it will be called by Akka
  def this(settings: ActorSystem.Settings, config: Config) = {
    // put your initialization code here
    this(Try { Class.forName(config.getString("filter")).newInstance().asInstanceOf[FaddishMailboxFilter]}.getOrElse(new FaddishMailboxFilter {}))
  }

  // The create method is called to create the MessageQueue
  final override def create(
                             owner:  Option[ActorRef],
                             system: Option[ActorSystem]): MessageQueue =
    new FaddishMessageQueue(filter)
}