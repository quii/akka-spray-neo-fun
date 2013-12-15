import akka.actor.Actor
import neo.NeoRESTApi._
import spray.http.HttpResponse


object TweetQuery {
  case class GetRelatedDocuments(doi: String)
}

class TweetQuery extends Actor {

  import TweetQuery._

  private val relatedTweets = {
    "MATCH (docs)-[:tweeted]-()-[:tweeted]-(basedoc) WHERE basedoc.name={doi} RETURN docs.name, COUNT(docs) ORDER BY COUNT(docs) DESC"
  }

  private val queryer = createNeoRestActor(context, "http://localhost:7474/db/data/cypher")

  def receive = {

    case GetRelatedDocuments(doi) => {
      queryer ! RunCypher(relatedTweets, Map("doi" -> doi))
    }

    case response:HttpResponse => {
      println("response = " + response)
    }
  }
}
