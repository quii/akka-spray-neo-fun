
import akka.actor.Actor
import neo.NeoRESTApi._
import akka.pattern.{ask, pipe}
import neo.NodeResponseParser
import scala.concurrent.Future
import spray.http.{HttpHeader, HttpResponse}

object TweetTrackerMessages{
  case class RecordTweet(person: String, doi: String)
}

class TweetTracker extends Actor{

  case class NodesInserted(person: HttpResponse, doi: HttpResponse)

  import TweetTrackerMessages._

  import context.dispatcher

  private val personCreator = createNeoRestActor(context, "http://localhost:7474/db/data/index/node/person?uniqueness=get_or_create")
  private val doiCreator = createNeoRestActor(context, "http://localhost:7474/db/data/index/node/document?uniqueness=get_or_create")
  private val relationshipCreator = createNeoRestActor(context)

  def receive = {
    case RecordTweet(person, doi) => {

      (for {
        p <- (personCreator ? CreateNode(NodeCreationBody("name", person, Map("name" -> person)))).mapTo[HttpResponse]
        d <- (doiCreator ? CreateNode(NodeCreationBody("doi", doi, Map("name" -> doi)))).mapTo[HttpResponse]
      }
      yield (NodesInserted(p, d))) pipeTo self

    }

    case NodesInserted(person, doi) => {

      import NodeResponseParser._

      val relationship = CreateRelationship(getRelationshipLocation(person), getLocation(doi), "tweeted")
      relationshipCreator ! relationship

    }

    case RelationshipCreatedOK => println("Person to DOI relationship created :)")

    case RelationshipCreatedFailed => println("Person to DOI relationship failed to create :(")

  }

}