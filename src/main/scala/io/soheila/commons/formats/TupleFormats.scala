package io.soheila.commons.formats

import play.api.data.validation._
import play.api.libs.json._

import scala.language.implicitConversions

/**
 * Implicits to format Tuple into JsonArray and Vice Versa.
 */
object TupleFormats {

  implicit def tuple2Reads[B, T1, T2](c: (T1, T2) => B)(implicit aReads: Reads[T1], bReads: Reads[T2]): Reads[B] = Reads[B] {
    case JsArray(arr) if arr.size == 2 => for {
      a <- aReads.reads(arr.head)
      b <- bReads.reads(arr(1))
    } yield c(a, b)
    case _ => JsError(Seq(JsPath() -> Seq(ValidationError("Expected array of two elements"))))
  }

  implicit def tuple2Writes[T1, T2](implicit aWrites: Writes[T1], bWrites: Writes[T2]): Writes[(T1, T2)] = new Writes[(T1, T2)] {
    def writes(tuple: (T1, T2)): JsArray = JsArray(Seq(aWrites.writes(tuple._1), bWrites.writes(tuple._2)))
  }

  implicit def tuple2Format[T1, T2](implicit aReads: Reads[T1], bReads: Reads[T2], aWrites: Writes[T1], bWrites: Writes[T2]): Format[(T1, T2)] = Format(tuple2Reads[(T1, T2), T1, T2]((t1, t2) => (t1, t2)), tuple2Writes[T1, T2])
}
