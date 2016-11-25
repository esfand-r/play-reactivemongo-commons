package io.soheila.commons.exceptions

object ErrorCode extends Enumeration {
  type Error = Value

  val GENERIC_DATABASE_ERROR = Value(1)
  val DUPLICATE_ENTITY = Value(11000)
}
