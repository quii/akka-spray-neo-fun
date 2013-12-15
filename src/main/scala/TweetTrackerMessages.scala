
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
  private val labelMaker = createNeoRestActor(context)

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

      val personLocations = getLocations(person)
      val doiLocations = getLocations(doi)

      val relationshipToMake: Option[CreateRelationship] = for {
        personLocations <- personLocations
        doiLocations <- doiLocations
        relationship = NodeRelationshipBody(doiLocations.location, "tweeted")
      }
      yield (CreateRelationship(personLocations.relationships, relationship))

      if(relationshipToMake.isEmpty){
        println("Something messed up when parsing responses")
        throw new Exception("balls")
      }

      relationshipCreator ! relationshipToMake.get
      labelMaker ! CreateLabel(personLocations.get.labels, "person")
      labelMaker ! CreateLabel(doiLocations.get.labels, "document")

    }

    case RelationshipCreatedOK => println("Person to DOI relationship created :)")

    case RelationshipCreatedFailed => println("Person to DOI relationship failed to create :(")

  }

}