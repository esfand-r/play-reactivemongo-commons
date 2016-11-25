package io.soheila.commons.entities

/**
 * Type trait providing audit info for entities.
 */
trait IdentityWithAudit[T, ID] extends Identity[T, ID] {
  /**
   * @param entity entity that needs audit information.
   * @return The entity enriched with createdOn and publishedOn.
   */
  def addAuditTrail(entity: T): T

  /**
   * @param entity entity that needs audit information updated.
   * @return The entity enriched with updated updatedOn.
   */
  def updateAuditTrail(entity: T): T
}
