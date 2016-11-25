package io.soheila.commons.crud

import javax.inject.Inject

import io.soheila.commons.geospatials.Geospatial
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Data Access Layer for the Strains resource.
 */
class CRUDTestDAOImpl @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends MongoCRUDDAO[TestEntity, String] with Geospatial[TestEntity, String] {
  override def collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("testStories"))

  override def indexSet: Set[Index] = {
    Set(Index(Seq("title" -> IndexType.Ascending), unique = true), geo2DSphericalIndex)
  }
}
