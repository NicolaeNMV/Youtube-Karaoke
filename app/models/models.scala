package models

import java.util.Date
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import models._

object Ressource {
  object Type extends Enumeration {
    type Type = Value
    val youtube = Value
  }

  def idFromURL(url: String): Option[String] = {
    """v=([\w-]{11})""".r.findFirstMatchIn(url).map(_.group(1))
  }

  def asEmbededURL(id: String): String = {
      s"http://www.youtube.com/embed/$id?feature=player_detailpage"
  }
}

case class Ressource(id: String, title: String, from: Ressource.Type.Value, created: Date) {
  lazy val embededURL: String = Ressource.asEmbededURL(id)
}

object RessourceJson {

  implicit object readRessourceType extends Reads[Ressource.Type.Value] {
    def reads(json: JsValue): JsResult[Ressource.Type.Value] = {
      JsSuccess(Ressource.Type.withName(json.as[String]))
    }
  }

  implicit val ressourceReader = (
    (__ \ 'id).read[String] and
    (__ \ 'title).read[String] and
    (__ \ 'from).read[Ressource.Type.Value] and
    (__ \ 'created \ '$date).read[Date]
  )((id, title, from, created) => Ressource(id, title, from, created))

  implicit val ressourcesReader = seq(ressourceReader)
}
