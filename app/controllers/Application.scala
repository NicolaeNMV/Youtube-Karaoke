package controllers

import scala.concurrent.Future
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current
import play.api.libs.ws._
import play.modules.reactivemongo._
import play.modules.reactivemongo.PlayBsonImplicits._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Application extends Controller {
  val db = ReactiveMongoPlugin.db
  lazy val collection = db("videos")

  def index = Action { implicit req =>
    Ok(views.html.index())
  }

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
}
