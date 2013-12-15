
import TweetQuery.GetRelatedDocuments
import TweetTrackerMessages.RecordTweet
import akka.actor.{Props, ActorSystem}
import scala.annotation.tailrec

object Commands {
  val QuitCommand   = "quit"
  val TrackCommand = "(.*) tweeted (.*)".r
  val RelatedCommand = "related (.*)".r
}

object Main extends App {

  import Commands._

  val system = ActorSystem()
  val tweetTracker = system.actorOf(Props(new TweetTracker()))
  val relatedTweetsFinder = system.actorOf(Props(new TweetQuery()))

  @tailrec
  private def commandLoop(): Unit = {
    Console.readLine() match {
      case QuitCommand         => {
        system.shutdown()
        return
      }
      case TrackCommand(person, doi) => tweetTracker ! RecordTweet(person, doi)
      case RelatedCommand(doi) => relatedTweetsFinder ! GetRelatedDocuments(doi)
      case _                   => println("WTF??!!")
    }

    commandLoop()
  }

  // start processing the commands
  commandLoop()

}
