package db

import scala.concurrent.Future
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import play.modules.reactivemongo._
import play.modules.reactivemongo.PlayBsonImplicits._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Play.current
import play.api.libs.json._
import models._

object Storage {

  val db = ReactiveMongoPlugin.db
  lazy val ressources = db("ressources")

  def newRessource(id: String, title: String, lyrics: Option[String], from: Ressource.Type.Value): Future[Unit] = {
    val lyricsAsJson = lyrics map { l =>
      Json.obj("lyrics" -> l)
    } getOrElse Json.obj()

    val json = Json.obj(
      "id" -> id,
      "title" -> title,
      "from" -> from.toString,
      "created" -> Json.obj("$date" -> new java.util.Date().getTime())
    ) ++ lyricsAsJson

    ressources.insert[JsValue]( json ).map(_ => Unit)
  }

  def findRessources(): Future[List[JsValue]] = {
    ressources.find[JsValue, JsValue](Json.obj()).toList
  }

  def findRessource(id: String): Future[Option[JsValue]] = {
    val byURL = Json.obj("id" -> id)
    ressources.find[JsValue, JsValue](byURL).headOption
  }
}
