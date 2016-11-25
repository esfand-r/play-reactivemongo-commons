package io.soheila.commons.entities

/**
 * Class representing a dynamic attributes.
 * A dynamic attribute is made of a String key and a sequence of string as values.
 */
case class Attribute(key: String, value: Seq[String])

object Attribute {
  import play.api.libs.json.Json

  implicit val jsonFormat = Json.format[Attribute]
}
