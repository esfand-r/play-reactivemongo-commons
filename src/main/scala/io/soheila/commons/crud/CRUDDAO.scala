package io.soheila.commons.crud

import io.soheila.commons.entities.Page
import io.soheila.commons.exceptions.MongoDAOException
import play.api.libs.json._
import reactivemongo.api.indexes.Index

import scala.concurrent.Future

/**
 * CRUD Data Access Trait.
 *
 * @tparam T  type of entity
 * @tparam ID type of identity of entity (primary key)
 */
trait CRUDDAO[T, ID] {

  /**
   * Returns the paginated list of entities without any criteria.
   *
   * @param page       Current page number (starts from 0)
   * @param totalCount Limit to 10 when not provided
   */
  def read(page: Int = 0, totalCount: Int = 10): Future[Either[MongoDAOException, Page[T]]]

  /**
   * Reads an entity by primary ID.
   *
   * @param id The ID of the entity to be read.
   * @return Some(Entity) if found and None if there was no entity with the given ID.
   */
  def read(id: ID): Future[Option[T]]

  /**
   * Create an entity.
   *
   * @param entity The entity to be created.
   * @return Either an error object or the created entity.
   */
  def create(entity: T): Future[Either[MongoDAOException, T]]

  /**
   * Create an entity.
   *
   * @param entity The entity to be created.
   * @return Either an error object or the created entity.
   */
  def create(id: ID, entity: T): Future[Either[MongoDAOException, T]]

  /**
   * Update an entity.
   *
   * @param id     The ID of the entity to be updated.
   * @param entity The entity to be updated.
   * @return Either an error object or the id of the updated entity.
   */
  def update(id: ID, entity: T): Future[Either[MongoDAOException, ID]]

  /**
   * Delete an entity.
   *
   * @param id The ID of the entity to be deleted.
   * @return Either an error object or the id of the deleted entity.
   */
  def delete(id: ID): Future[Either[MongoDAOException, ID]]

  /**
   * find entities with json criteria.
   *
   * @param criteria The search criteria in json.
   * @param limit    Maximum number of records to look for.
   * @return a Traversable of found entities.
   */
  def find(criteria: JsObject, limit: Int): Future[Traversable[T]]

  /**
   * find entities with json criteria.
   *
   * @param criteria   The search criteria in json.
   * @param limit      Maximum number of records to look for.
   * @param sortFilter filter to be used for sorting. sortFilter._1 is the name of the filed to be sorted and sortFilter._2 is the sort direction.
   *                   if an option is provided, ._1 must be a notBlank string and ._2 must be either 1 or -1.
   * @return page of found entities.
   */
  def find(criteria: JsObject, page: Int = 0, limit: Int, sortFilter: Option[(String, Int)]): Future[Either[MongoDAOException, Page[T]]]

  /**
   * find an entity with json criteria.
   *
   * @param criteria The search criteria in json.
   * @return an entity matching the criteria.
   */
  def findOne(criteria: JsObject): Future[Either[MongoDAOException, Option[T]]]

  /**
   * find an entity with json criteria and updates it.
   *
   * @param criteria The search criteria in json.
   * @return an entity matching the criteria.
   */
  def findAndUpdateByCriteria(criteria: JsObject, entity: T, upsert: Boolean = false): Future[Either[MongoDAOException, Option[T]]]

  /**
   * returns collection indexes.
   *
   * @return set of indexes for the collection of T.
   */
  def indexSet: Set[Index]
}
