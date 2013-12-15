package neo

import spray.http.HttpResponse
import spray.json._
import DefaultJsonProtocol._
import neo.NeoRESTApi.{RunCypher, NodeRelationshipBody, NodeCreationBody}
import scala.util.{Success, Try}

object NodeResponseParser {

  case class NodeRelationShip(outgoing_relationships: String){
    def relationshipPostUrl = outgoing_relationships.replaceAllLiterally("/out", "")
  }
  case class NodeLabels(labels: String)
  case class NodeLocations(location: String, relationships: String, labels: String)

  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val locationFormat = jsonFormat1(NodeRelationShip)
    implicit val labelFormat = jsonFormat1(NodeLabels)
    implicit val createFormat = jsonFormat3(NodeCreationBody)
    implicit val relationshipFormat = jsonFormat2(NodeRelationshipBody)
    implicit val cypherFormat = jsonFormat2(RunCypher)
  }


  def getLocations(res: HttpResponse): Option[NodeLocations] = {

    import MyJsonProtocol._

    val responseString: String = res.entity.asString

    val location = res.headers.find(_.is("location")).map(_.value)
    val relationship = Try(responseString.asJson.convertTo[NodeRelationShip]).toOption
    val label = Try(responseString.asJson.convertTo[NodeLabels]).toOption

    for{
      l <- location
      r <- relationship
      lb <- label
    }
    yield NodeLocations(l, r.relationshipPostUrl, lb.labels)

  }
}
