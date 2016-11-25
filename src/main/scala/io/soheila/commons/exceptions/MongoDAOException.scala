package io.soheila.commons.exceptions

import io.soheila.commons.exceptions.ErrorCode.Error

case class MongoDAOException(message: String, errorCode: Error, code: Int, cause: Throwable) extends RuntimeException(message, cause)
