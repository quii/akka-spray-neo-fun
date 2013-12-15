package neo

import akka.actor.{Props, ActorContext, Actor}
import akka.io.IO
import spray.can.Http
import spray.http._
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.{ask, pipe}
import spray.client.pipelining._
import spray.http.HttpRequest
import spray.http.HttpResponse
import scala.concurrent.Future
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport._
import neo.NodeResponseParser.MyJsonProtocol

object NeoRESTApi {
  case class NodeCreationBody(key: String, value: String, properties: Map[String, String])
  case class CreateNode(query: NodeCreationBody)

  case class NodeRelationshipBody(to: String, `type`: String)
  case class CreateRelationship(uri: String, relationship: NodeRelationshipBody)
  case object RelationshipCreatedOK
  case object RelationshipCreatedFailed

  implicit val timeout = Timeout(5 seconds)

  def createNeoRestActor(implicit context: ActorContext, baseUrl:String = "http://localhost:7474/db/data") = {
    context.actorOf(Props(new NeoRESTApi(baseUrl)))
  }
}

class NeoRESTApi(baseUrl: String) extends Actor {

  import NeoRESTApi._

  implicit val system = context.system
  import context.dispatcher
  import MyJsonProtocol._

  val pipeline: HttpRequest => Future[HttpResponse] = (
    addHeader("Accept", "application/json")
      ~> sendReceive
    )

  def receive = {

    case CreateNode(query) => {
      pipeline(Post(baseUrl, query)).mapTo[HttpResponse] pipeTo sender
    }

    case CreateRelationship(uri, relationship) =>{

      val response = pipeline(Post(uri, relationship)).mapTo[HttpResponse]

      response.map(r=> {
        if(r.status.isSuccess) RelationshipCreatedOK else RelationshipCreatedFailed
      }) pipeTo sender
    }

  }

}


