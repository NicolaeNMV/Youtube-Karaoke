package controllers

import scala.concurrent.Future
import scala.util.Try
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.data._
import play.api.data.Forms._
import org.jsoup.Jsoup
import models._
import db._

object Application extends Controller {

  def index = Action { implicit req =>
    import RessourceJson._
    Async {
      Storage.findRessources().map(_.map(_.as[Ressource])).map { ressources =>
        Ok(views.html.index(ressources))
      }
    }
  }

  def redirectToIndex = Redirect(routes.Application.index)

  val ressourceForm = Form(
    "url" -> nonEmptyText
  )

  def newRessource() = Action { implicit request =>
    Async {
      ressourceForm.bindFromRequest.fold(
        errors => Future.successful(redirectToIndex),
        url => Try {
          val id = Ressource.idFromURL(url).get
          (for {
            response <- WS.url(url).get()
            maybeRessource <- Storage.findRessource(id)
          } yield {
            if(!maybeRessource.isDefined) {
              val html = Jsoup.parse(response.body)
              val title = html.select("#eow-title").text()
              Some(title)
            } else None
          }).flatMap { maybeLinks =>
            maybeLinks.map { title =>
              Lyrics.url(title).flatMap { maybeLyricsLink =>
                Storage.newRessource(id, title, maybeLyricsLink, Ressource.Type.youtube) map { _ =>
                  redirectToIndex
                }
              }
            }.getOrElse(Future.successful(redirectToIndex))
          }.recover {
            case e:Exception => redirectToIndex
          }
        }.toOption.getOrElse(Future.successful(redirectToIndex))
      )
    }
  }

  def ressource(id: String) = Action { implicit request =>
    import RessourceJson._
    Async {
      (for {
        ressource <- Storage.findRessource(id).map(_.map(json => json.as[Ressource]))
        if(ressource.isDefined)
        if(ressource.get.lyrics.isDefined)
        lyrics <- Lyrics.byURL(ressource.get.lyrics.get)
      } yield {
        Ok(views.html.ressource(ressource.get, lyrics))
      }).recover {
        case e:Exception => BadRequest
      }
    }
  }
}
