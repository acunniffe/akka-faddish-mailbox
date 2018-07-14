# Akka Faddish Mailbox

> faddish: Something that's faddish is in style, often for a brief length of time.

Use case: Long running tasks are very expensive to queue up, especially if their results are no longer valid. This library allow you check ahead of the message being added to the queue and delete messages that are now invalid before they are processed.

It's also expensive to check/filter all the messages already in an Akka Queue so there's no built-in functionality for this. If you're considering using this library the cost of checking for duplicate/now-invalid tasks ahead of your new message should be less than the cost of running those tasks anyway.

At Optic we use Actors as part of our pipeline to parse/analyse source code. When a user edits their code new messages are queued. Due to the nature of our connections to IDEs debouncing wasn't feasible. For a while we were parsing many variants of the same file and throwing out the old versions. This custom mailbox ensures that we do the minimal amount of work every time.

## Usage
1. Include from maven `"com.opticdev.akka" %% "akka-faddish-mailbox" % "0.1.0"`
2. Create an instance of `FaddishMailboxFilter` and override the `filterOut` method. This method takes the latest message being added to the queue and returns a `PartialFunction[Any, Boolean]` that is used by an internal filter method to take messages out of the queue that match your predicates.
```scala
//If all of these are queued before the first message completes then..
//Ping("red", 10) -> Processed
//Ping("red", 20) -> Ignored
//Ping("red", 30) -> Ignored
//Ping("red", 40) -> Processed

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
```
3. Implement `RequiresMessageQueue[FaddishUnboundedMessageQueueSemantics]` in your Actor. No other changes to the actor are needed.
```scala
class TestActor extends Actor with RequiresMessageQueue[FaddishUnboundedMessageQueueSemantics] {
```
4. Add a record to your `application.conf` file. Make sure this library and your filter is in the same classpath.
```scala
my-mailbox {
  mailbox-type = "com.opticdev.scala.akka.FaddishUnboundedMailbox"
  filter = "com.opticdev.scala.akka.TestFilter"
}
```

5. Create your actor. Pass the name you used in `application.conf`
```scala
val actorRef = system.actorOf(Props[TestActor].withDispatcher("my-mailbox"))
```

## Router Logic
If you use a standard Akka router the Faddish Mailbox won't do you much good. If Worker A gets Doc1 v1, then Worker B gets Doc1 v2, etc you end up with the same problem as before. For this reason we've included our own RouterLogic called `HashDispatchedRoutingLogic`.

This routing logic behaves the same as a random router with one key difference: it behaves deterministically for messages with a unique key. `HashDispatchedRoutingLogic` guarantees that messages with the same unique keys (ie ones processing /path/to/fileA) always are routed to the same worker as they come in. The algorithm also guarantees a quasi-uniform distribution of keys (k) over any number of workers (w) to avoid processing biases.

Each instance of `HashDispatchedRoutingLogic` takes an function that gets unique keys from the various message types your router needs to support. If you return `None` than that message type will be randomly delegated to a worker.

```scala
HashDispatchedRoutingLogic({
    case a: ParseJob => Some(a.filePath)
    case _ => None
})
```

Then create your router:
```scala
class ParseSupervisorActor()(implicit actorCluster: ActorCluster) extends Actor {
    var router = {
        val routees = Vector.fill(SGConstants.parseWorkers) {
          val r = context.actorOf(WorkerActor.props())
          context watch r
          ActorRefRoutee(r)
        }
        Router(HashDispatchedRoutingLogic({
                   case a: ParseJob => Some(a.filePath)
                   case _ => None
        }), routees)
    }
...
}
```


## License
MIT
