package io.soheila.commons.entities

import io.soheila.commons.geospatials.Coordinate

trait Locatable {
  /**
   * It is used to record the geolocation of an entity.
   *
   * @return The coordinates of an entity.
   */
  def coordinate: Coordinate
}
