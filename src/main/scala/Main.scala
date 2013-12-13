
import TweetTrackerMessages.RecordTweet
import akka.actor.{Props, ActorSystem}
import scala.annotation.tailrec

object Commands {
  val QuitCommand   = "quit"
  val TrackCommand = "(.*) tweeted (.*)".r
}

object Main extends App {

  import Commands._

  val system = ActorSystem()
  val tweetTracker = system.actorOf(Props(new TweetTracker()))

  @tailrec
  private def commandLoop(): Unit = {
    Console.readLine() match {
      case QuitCommand         => {
        system.shutdown()
        return
      }
      case TrackCommand(person, doi) => tweetTracker ! RecordTweet(person, doi)
      case _                   => println("WTF??!!")
    }

    commandLoop()
  }

  // start processing the commands
  commandLoop()

}
