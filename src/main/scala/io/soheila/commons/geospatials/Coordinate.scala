package io.soheila.commons.geospatials

import io.soheila.commons.formats.TupleFormats
import play.api.libs.json.{ Format, JsArray, Reads, Writes }

case class Coordinate(lon: Double, lat: Double) {
  lazy val tuple: (Double, Double) = (lon, lat)
}

object Coordinate {

  val reads: Reads[Coordinate] = TupleFormats.tuple2Reads[Coordinate, Double, Double](Coordinate.apply)

  def writes(implicit aWrites: Writes[Double]) = new Writes[Coordinate] {
    def writes(coordinates: Coordinate) = JsArray(Seq(aWrites.writes(coordinates.lon), aWrites.writes(coordinates.lat)))
  }

  implicit val formats = Format(reads, writes)
}
