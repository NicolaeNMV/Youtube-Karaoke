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

  def newRessource(id: String, title: String, from: Ressource.Type.Value): Future[Unit] = {
    val json = Json.obj(
      "id" -> id,
      "title" -> title,
      "from" -> from.toString,
      "created" -> Json.obj("$date" -> new java.util.Date().getTime())
    )
    ressources.insert[JsValue]( json ).map(_ => Unit)
  }

  def findRessources(): Future[List[JsValue]] = {
    ressources.find[JsValue, JsValue](Json.obj()).toList
  }

  def findRessource(url: String): Future[Option[JsValue]] = {
    val byURL = Json.obj("url" -> url)
    ressources.find[JsValue, JsValue](byURL).headOption
  }
}