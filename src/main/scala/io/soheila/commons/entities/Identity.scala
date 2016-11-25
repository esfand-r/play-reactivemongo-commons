package io.soheila.commons.entities

/**
 * Type trait providing identity manipulation methods.
 */
trait Identity[T, ID] {

  /**
   * It is used to identify the name of the field that is used as primary ID.
   *
   * @return The name identifying the primary ID.
   */
  def name: String

  /**
   * @param entity Type of the entity.
   * @return The primary ID.
   */
  def of(entity: T): Option[ID]

  /**
   * @param entity The entity.
   * @param id     Type of the primary ID.
   * @return The entity.
   */
  def set(entity: T, id: ID): T

  /**
   * @param entity Type of the entity.
   * @return The entity without its primary ID. This will usually be used before calling save in a DAO.
   */
  def clear(entity: T): T

  /**
   * @return The primary ID to be used for creating the next entity.
   */
  def newID: ID
}
