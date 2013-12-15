package neo

import spray.http.HttpResponse
import spray.json._
import DefaultJsonProtocol._
import neo.NeoRESTApi.{RunCypher, NodeRelationshipBody, NodeCreationBody}

object NodeResponseParser {

  case class RelationshipLocation(outgoing_relationships: String)

  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val locationFormat = jsonFormat1(RelationshipLocation)
    implicit val createFormat = jsonFormat3(NodeCreationBody)
    implicit val relationshipFormat = jsonFormat2(NodeRelationshipBody)
    implicit val cypherFormat = jsonFormat2(RunCypher)
  }


  def getRelationshipLocation(res: HttpResponse) = {

    import MyJsonProtocol._

    val location = res.entity.asString.asJson.convertTo[RelationshipLocation]

    location.outgoing_relationships.replaceAllLiterally("/out", "")

  }

  def getLocation(res: HttpResponse) = {
    res.headers.find(_.is("location")).map(_.value).getOrElse("")
  }
}
