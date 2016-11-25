package io.soheila.commons.exceptions

import reactivemongo.api.commands.LastError

object MongoExceptionBuilder {
  val UnknownErrorMessage = "No Error Message"

  def buildException(databaseException: Throwable): MongoDAOException = {
    databaseException match {
      case e @ (_: LastError) => buildResponseForLastError(e)
      case e => MongoDAOException(e.getMessage, ErrorCode.GENERIC_DATABASE_ERROR, 0, e)
    }
  }

  private def buildResponseForLastError(databaseException: LastError): MongoDAOException = {
    databaseException.code.getOrElse(0) match {
      case code if code == 11000 => MongoDAOException(databaseException.errmsg.getOrElse("Duplicate Entity"), ErrorCode.DUPLICATE_ENTITY, code, databaseException)
      case code => MongoDAOException(databaseException.errmsg.getOrElse(UnknownErrorMessage), ErrorCode.GENERIC_DATABASE_ERROR, code, databaseException)
    }
  }
}
