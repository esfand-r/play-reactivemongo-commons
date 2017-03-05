package io.soheila.commons.crud

import grizzled.slf4j.Logger
import io.soheila.commons.entities.{ Identity, IdentityWithAudit, Page }
import io.soheila.commons.exceptions.{ MongoDAOException, MongoExceptionBuilder }
import play.api.libs.json.{ Format, JsObject, Json }
import reactivemongo.api._
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Abstract {{CRUDService}} impl backed by JSONCollection providing some convenience for DAOs.
 */
abstract class MongoCRUDDAO[T: Format, ID: Format](
  implicit
  val identity: Identity[T, ID],
  implicit val ec: ExecutionContext
) extends CRUDDAO[T, ID] {
  private val logger = Logger[this.type]

  def collection: Future[JSONCollection]

  collection onSuccess {
    case jsonCollection => indexSet.foreach(
      index => jsonCollection.indexesManager.ensure(index)
    )
  }

  override def read(page: Int = 0, limit: Int): Future[Either[MongoDAOException, Page[T]]] = {
    require(page >= 0, s"Page must be greater or equal to 0, got $page")
    require(limit > 0, s"Page size must be more then 0, got $limit")

    val offset = page * limit

    val totalCount = collection.flatMap(_.count())

    val filtered = collection.flatMap(_.find(Json.obj())
      .options(QueryOpts(skipN = page * limit))
      .cursor[T]()
      .collect[Seq](limit, Cursor.FailOnError((_: Seq[T], err) => logger.error(err.getMessage, err))))

    totalCount.zip(filtered).map {
      case (total, entities) =>
        Right(Page(entities, page, offset, total))
    }.recover {
      case e => Left(MongoExceptionBuilder.buildException(e))
    }
  }

  override def read(id: ID): Future[Option[T]] = collection.flatMap(_.find(Json.obj(identity.name -> id)).one[T])

  override def create(entity: T): Future[Either[MongoDAOException, T]] = {
    find(Json.toJson(
      identity.clear(entity)
    ).as[JsObject], 1).flatMap {
      case t if t.nonEmpty =>
        Future.successful(Right(t.head)) // Success if all the fields are the same as existing record staying idempotent.
      case _ =>
        val id = identity.newID

        val entityWithNewId: T = identity.set(entity, id)

        val entityToCreate = identity match {
          case i: IdentityWithAudit[T, ID] => i.addAuditTrail(entityWithNewId)
          case _ => entityWithNewId
        }

        val doc = Json.toJson(entityToCreate).as[JsObject]

        collection.flatMap(_.insert(doc) map (_ => Right(entityToCreate))).recover {
          case e => Left(MongoExceptionBuilder.buildException(e))
        }
    }
  }

  override def create(id: ID, entity: T): Future[Either[MongoDAOException, T]] = {
    find(Json.toJson(
      identity.clear(entity)
    ).as[JsObject], 1).flatMap {
      case t if t.nonEmpty =>
        Future.successful(Right(t.head)) // Success if all the fields are the same as existing record staying idempotent.
      case _ =>
        val entityWithNewId: T = identity.set(entity, id)

        val entityToCreate = identity match {
          case i: IdentityWithAudit[T, ID] => i.addAuditTrail(entityWithNewId)
          case _ => entityWithNewId
        }

        val doc = Json.toJson(entityToCreate).as[JsObject]

        collection.flatMap(_.insert(doc) map (_ => Right(entityToCreate))).recover {
          case e => Left(MongoExceptionBuilder.buildException(e))
        }
    }
  }

  override def update(id: ID, entity: T): Future[Either[MongoDAOException, ID]] = {
    val entityWithNewId: T = identity.set(entity, id)

    val entityToUpdate = identity match {
      case i: IdentityWithAudit[T, ID] => i.updateAuditTrail(entityWithNewId)
      case _ => entityWithNewId
    }

    val doc = Json.toJson(entityToUpdate).as[JsObject]
    collection.flatMap(_.update(Json.obj(identity.name -> id), doc) map (_ => Right(id))).recover {
      case e => Left(MongoExceptionBuilder.buildException(e))
    }
  }

  override def delete(id: ID): Future[Either[MongoDAOException, ID]] = {
    collection.flatMap(_.remove(Json.obj(identity.name -> id)) map (_ => Right(id))).recover {
      case e => Left(MongoExceptionBuilder.buildException(e))
    }
  }

  override def find(criteria: JsObject, limit: Int): Future[Traversable[T]] = {
    collection.flatMap(_.find(criteria).
      cursor[T](readPreference = ReadPreference.nearest).
      collect[Seq](limit, Cursor.FailOnError((_: Seq[T], err) => logger.error(err.getMessage, err))))
  }

  override def find(criteria: JsObject, page: Int = 0, limit: Int, sortFilter: Option[(String, Int)]): Future[Either[MongoDAOException, Page[T]]] = {
    require(page >= 0, s"Page must be greater or equal to 0, got $page")
    require(limit > 0, s"Page size must be more then 0, got $limit")

    val offset = page * limit

    val internalSortFilter = sortFilter match {
      case Some(filter) =>
        require(!Option(filter._1).getOrElse("").isEmpty, "When Filter is provided, field to use for sort is also required.")
        require(filter._2 == -1 || filter._2 == 1, "For sorting direction please use 1 for Ascending and 0 for Descending")
        Json.obj(filter._1 -> filter._2)
      case None => Json.obj()
    }

    val filtered = collection.flatMap(_.find(criteria).options(QueryOpts(skipN = page * limit))
      .sort(internalSortFilter)
      .cursor[T]()
      .collect[Seq](limit, Cursor.FailOnError((_: Seq[T], err) => logger.error(err.getMessage, err))))

    val totalCount = collection.flatMap(_.count())

    totalCount.zip(filtered).map {
      case (total, entities) =>
        Right(Page(entities, page, offset, total))
    }.recover {
      case err => Left(MongoExceptionBuilder.buildException(err))
    }
  }

  override def findOne(criteria: JsObject): Future[Either[MongoDAOException, Option[T]]] = {
    collection.flatMap(_.find(criteria).one[T]).map(le => Right(le))
      .recover {
        case err => Left(MongoExceptionBuilder.buildException(err))
      }
  }

  override def findAndUpdateByCriteria(criteria: JsObject, entity: T, upsert: Boolean): Future[Either[MongoDAOException, Option[T]]] = {
    val doc = Json.toJson(entity).as[JsObject]

    collection.flatMap(_.findAndUpdate(criteria, doc, fetchNewObject = true, upsert)
      map (le => Right(le.result[T]))).recover {
      case err => Left(MongoExceptionBuilder.buildException(err))
    }
  }
}
