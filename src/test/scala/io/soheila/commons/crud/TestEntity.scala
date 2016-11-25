package io.soheila.commons.crud

import java.time.LocalDateTime

import com.fasterxml.uuid.Generators
import io.soheila.commons.entities.{ Attribute, IdentityWithAudit, Locatable }
import io.soheila.commons.formats.EnumFormat
import io.soheila.commons.geospatials.Coordinate
import play.api.libs.json.{ Format, OFormat }

object StoryType extends Enumeration {
  type StoryType = Value

  val Article = Value("Article")
  val UserPost = Value("UserPost")
  val GuestPost = Value("GuestPost")
  val BasicPage = Value("BasicPage")
  val Research = Value("Research")
}

case class TestEntity(
  uuid: Option[String],
  createdOn: LocalDateTime,
  updatedOn: LocalDateTime,
  storyType: StoryType.Value,
  title: String,
  attributes: Seq[Attribute],
  override val coordinate: Coordinate
) extends Locatable

object TestEntity {

  import play.api.libs.json.Json

  implicit val storyTypeFormat: Format[StoryType.Value] = EnumFormat.enumFormat(StoryType)
  implicit val jsonFormat: OFormat[TestEntity] = Json.format[TestEntity]

  implicit object TestEntityIdentity extends IdentityWithAudit[TestEntity, String] {
    val name = "uuid"

    override def of(entity: TestEntity): Option[String] = entity.uuid

    override def set(entity: TestEntity, uuid: String): TestEntity = entity.copy(uuid = Option(uuid))

    override def clear(entity: TestEntity): TestEntity = entity.copy(uuid = None)

    override def newID: String = Generators.timeBasedGenerator().generate().toString

    override def addAuditTrail(entity: TestEntity): TestEntity = entity.copy(createdOn = LocalDateTime.now())

    override def updateAuditTrail(entity: TestEntity): TestEntity = entity.copy(updatedOn = LocalDateTime.now())
  }

}

