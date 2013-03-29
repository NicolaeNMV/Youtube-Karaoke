package controllers

import play.api._
import play.api.mvc._

// Reactive Mongo imports
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers._

// Reactive Mongo plugin
import play.modules.reactivemongo._
import play.modules.reactivemongo.PlayBsonImplicits._

// Play Json imports
import play.api.libs.json._

import play.api.Play.current

object Application extends Controller with MongoController {
  val db = ReactiveMongoPlugin.db
  lazy val collection = db("videos")

  def index = Action { Ok("works") }

  // creates a new Person building a JSON from parameters
  def addUrl(url: String) = Action {
    Async {
      val homePage: Future[play.api.libs.ws.Response] = WS.url("http://mysite.com").get()
      val json = Json.obj(
        "url" -> url,
        "created" -> new java.util.Date().getTime()
      )

      collection.insert[JsValue]( json ).map( lastError =>
        Ok("Mongo LastErorr:%s".format(lastError))
      )
    }
  }

  // queries for a person by name
  /*def findByName(name: String) = Action {
    Async {
      val qb = QueryBuilder().query(Json.obj( "name" -> name )).sort( "created" -> SortOrder.Descending)

      collection.find[JsValue]( qb ).toList.map { persons =>
        Ok(persons.foldLeft(JsArray(List()))( (obj, person) => obj ++ Json.arr(person) ))
      }
    }
  }*/ 

}