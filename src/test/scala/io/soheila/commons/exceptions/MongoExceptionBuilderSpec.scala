package io.soheila.commons.exceptions

import org.specs2.Specification
import reactivemongo.api.commands.LastError

class MongoExceptionBuilderSpec extends Specification {
  override def is =
    s2"""
 This is a specification to check error handling in CRUDDAO
   when exception of type LastError is raised for duplicate insert,
      MongoDAOException with Duplicate_Entry errorCode should be returned   $duplicateEntry
   when exception of type LastError is raised in general,
      then MongoDAOException should be raised containing the error message from reactivemongolayer $lastError
   when exception of some type other than LastError is raised,
      then MongoDAOException should be raised containing the message from underlying exception and general error code $generalException
   """

  def duplicateEntry = {
    val lastError = LastError(ok = true, Some("duplicate error message"), Some(11000), None, 5, None, updatedExisting = false, None, None, wtimeout = false, None, None)
    val daoException = MongoExceptionBuilder.buildException(lastError)

    daoException.errorCode must beEqualTo(ErrorCode.DUPLICATE_ENTITY)
    daoException.message must beEqualTo("duplicate error message")
  }

  def lastError = {
    val lastError = LastError(ok = true, Some("error message"), Some(100), None, 5, None, updatedExisting = false, None, None, wtimeout = false, None, None)
    val daoException = MongoExceptionBuilder.buildException(lastError)

    daoException.code must beEqualTo(100)
    daoException.message must beEqualTo("error message")
  }

  def generalException = {
    val exception = new Exception("error message")
    val daoException = MongoExceptionBuilder.buildException(exception)

    daoException.code must beEqualTo(0)
    daoException.message must beEqualTo("error message")
  }
}
