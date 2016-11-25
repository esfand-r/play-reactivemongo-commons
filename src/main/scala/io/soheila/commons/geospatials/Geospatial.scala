package io.soheila.commons.geospatials

import grizzled.slf4j.Logger
import io.soheila.commons.crud.MongoCRUDDAO
import io.soheila.commons.entities.{ Locatable, Page }
import io.soheila.commons.exceptions.{ ErrorCode, MongoDAOException, MongoExceptionBuilder }
import play.api.libs.json.{ Json, Reads }
import play.modules.reactivemongo.json._
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Geo2DSpherical
import reactivemongo.api.{ Cursor, ReadPreference }

import scala.concurrent.{ ExecutionContext, Future }

trait Geospatial[T <: Locatable, ID] {
  self: MongoCRUDDAO[T, ID] =>

  private val logger = Logger[this.type]

  lazy val LocationField = "coordinate"

  lazy val geo2DSphericalIndex = Index(Seq((LocationField, Geo2DSpherical)), Some("geo2DSphericalIdx"))

  /**
   * Returns the paginated list of entities near a point with latitude and longitude.
   *
   * @param lon            longitude of the point.
   * @param lat            latitude of the point.
   * @param minDistance    minimum distance to the point. Defaults to 1 meter when not provided.
   * @param maxDistance    maximum distance to the point. Defaults to 1 meter when not provided.
   * @param page           page number.
   * @param limit          Number of records to be returned
   * @param readPreference read preference setting for the query [[https://docs.mongodb.com/manual/core/read-preference/ MongoDB Read Preference]].
   */
  def nearPoint(lon: Double, lat: Double, minDistance: Double = 1, maxDistance: Double = 10000, page: Int = 0, limit: Int = 100, readPreference: ReadPreference = ReadPreference.primaryPreferred)(implicit ec: ExecutionContext, reads: Reads[T]): Future[Either[MongoDAOException, Page[T]]] = {
    val offset = page * limit

    val totalCount = collection.flatMap(_.count())

    val searchResult = collection.flatMap(_.find(
      Json.obj(
        LocationField -> Json.toJson(Json.obj(
          "$near" -> Json.toJson(Json.obj(
            "$geometry" -> Json.toJson(Json.obj(
              "type" -> "Point",
              "coordinates" -> Json.arr(lon, lat)
            )),
            "$maxDistance" -> Json.toJson(maxDistance),
            "$minDistance" -> Json.toJson(minDistance)
          ))
        ))
      )
    ).cursor[T](readPreference).collect[Seq](limit, Cursor.FailOnError((seq: Seq[T], err) => logger.error("Error finding locations.", err))))

    totalCount.zip(searchResult).map {
      case (total, entities) =>
        Right(Page(entities, page, offset, total))
    }.recover {
      case e => Left(MongoExceptionBuilder.buildException(e))
    }
  }

}
