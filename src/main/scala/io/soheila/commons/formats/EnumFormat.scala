package io.soheila.commons.formats

import play.api.libs.json._

import scala.language.implicitConversions

/**
 * Implicits to read and write enums to and from json.
 */
object EnumFormat {
  implicit def enumReads[A](enum: Enumeration): Reads[A] = new Reads[A] {
    def reads(json: JsValue): JsResult[A] = json match {
      case JsString(s) =>
        try {
          JsSuccess(enum.withName(s).asInstanceOf[A])
        } catch {
          case _: NoSuchElementException =>
            JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not contain '$s'")
        }
      case _ => JsError("JSon String value is required.")
    }
  }

  implicit def enumWrites[A]: Writes[A] = new Writes[A] {
    def writes(v: A): JsValue = JsString(v.toString)
  }

  implicit def enumFormat[A](enum: Enumeration): Format[A] = {
    Format(enumReads(enum), enumWrites)
  }
}

