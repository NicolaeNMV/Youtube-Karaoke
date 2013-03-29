package models

import scala.concurrent.Future
import java.util.Date
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws._
import db._
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

case class Ressource(id: String, lyrics: Option[String], syncs: Option[JsValue], title: String, from: Ressource.Type.Value, created: Date) {
  lazy val embededURL: String = Ressource.asEmbededURL(id)

  def saveSync(json: JsValue): Future[Unit] = Storage.saveSync(id, json)
}

object RessourceJson {

  implicit object readRessourceType extends Reads[Ressource.Type.Value] {
    def reads(json: JsValue): JsResult[Ressource.Type.Value] = {
      JsSuccess(Ressource.Type.withName(json.as[String]))
    }
  }

  implicit val ressourceReader = (
    (__ \ 'id).read[String] and
    (__ \ 'lyrics).readNullable[String] and
    (__ \ 'syncs).readNullable[JsValue] and
    (__ \ 'title).read[String] and
    (__ \ 'from).read[Ressource.Type.Value] and
    (__ \ 'created \ '$date).read[Date]
  )((id, lyrics, syncs, title, from, created) => Ressource(id, lyrics, syncs, title, from, created))

  implicit val ressourcesReader = seq(ressourceReader)
}


object Lyrics {

  def url(title: String): Future[Option[String]] = {
    WS.url("https://ajax.googleapis.com/ajax/services/search/web")
      .withQueryString("c" -> "")
      .withQueryString("v" -> "1.0")
      .withQueryString("q" -> s"""site:lyrics.wikia.com $title -"Page+Ranking+Information"""")
      .get().map(_.json).map { json =>
         (json \ "responseData" \ "results").as[List[JsValue]].headOption.flatMap(json => (json \ "unescapedUrl").asOpt[String])
       }
  }

  def byURL(url: String): Future[String] = {
    import org.jsoup.Jsoup

    WS.url(url)
      .get().map(_.body).map { page =>
         val html = Jsoup.parse(page)
         val lyrics = html.select(".lyricbox")
         lyrics.select("a").remove()
         lyrics.select(".rtMatcher").remove()
         lyrics.select(".lyricsbreak").remove()
         lyrics.select("#comments").remove()
         lyrics.toString
       }
  }
}
