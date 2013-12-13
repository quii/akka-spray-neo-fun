
import akka.actor.Actor
import neo.NeoRESTApi._
import akka.pattern.{ask, pipe}
import scala.concurrent.Future
import spray.http.{HttpHeader, HttpResponse}

object TweetTrackerMessages{
  case class RecordTweet(person: String, doi: String)
}

class TweetTracker extends Actor{

  case class NodesInserted(person: HttpResponse, doi: HttpResponse)

  import TweetTrackerMessages._

  import context.dispatcher

  private val personCreator = createNeoRestActor(context)
  private val doiCreator = createNeoRestActor(context)
  private val relationshipCreator = createNeoRestActor(context)



  def receive = {
    case RecordTweet(person, doi) => {

      println(s"Going to try and post $person and $doi to graph")

      (for {
        p <- (personCreator ? CreateNode(s"""{"name":"$person"}""")).mapTo[HttpResponse]
        d <- (doiCreator ? CreateNode(s"""{"name":"$doi"}""")).mapTo[HttpResponse]
      }
      yield (NodesInserted(p, d))) pipeTo self

    }

    case NodesInserted(person, doi) => {

      def getLocation(r: HttpResponse) = r.headers.find(_.is("location")).map(_.value)

      val relationship: Option[CreateRelationship] = for {
        personLocation <- getLocation(person)
        doiLocation <- getLocation(doi)
      } yield (CreateRelationship(personLocation, doiLocation, "tweeted"))

      if(relationship.isEmpty) throw new Exception("Failed to write nodes & relationship")

      relationshipCreator ! relationship.get

    }

    case RelationshipCreatedOK => println("Person to DOI relationship created :)")

    case RelationshipCreatedFailed => println("Person to DOI relationship failed to create :(")

  }
}