package io.soheila.commons.crud

import java.time.LocalDateTime
import java.util.UUID

import io.soheila.commons.{ MongoScope, MongoSpecification, WithMongo }
import io.soheila.commons.geospatials.Coordinate
import play.api.libs.json.Json
import play.api.test.{ PlaySpecification, WithServer }

import scala.concurrent.ExecutionContext.Implicits.global

class CRUDDAOSpec extends PlaySpecification with MongoSpecification {
  val aldgate = (-0.0770733, 51.5134224)
  val stPauls = (-0.0974016, 51.5146721)
  val barbican = (-0.090796, 51.512787)
  val londonBridge = (-0.0906243, 51.5038924)

  "The `create` method" should {
    "add a new entity" in new WithMongo("test") with Context {
      // Check to see if Right is returned which happens only when no error was returned.
      val initialStory = createEntity(aldgate, "title", "title")

      val story = await(testDAO.create(initialStory)).right.get

      val savedStory = await(testDAO.read(story.uuid.get)).get

      savedStory.storyType must beEqualTo(StoryType.Article)
      savedStory.uuid.get must beEqualTo(story.uuid.get)
    }

    "add a new entity by the provided UUID" in new WithMongo("test") with Context {
      // Check to see if Right is returned which happens only when no error was returned.
      val uuid = UUID.randomUUID().toString

      val date = LocalDateTime.now()
      val entity = TestEntity(None, date, date, StoryType.Article, "title", Seq(), Coordinate(-0.0770733, 51.5134224))

      val story = await(testDAO.create(uuid, entity))

      val savedStory = await(testDAO.read(uuid)).get

      savedStory.uuid.get must beEqualTo(uuid)
    }
  }

  "The `find` method" should {
    "find an entity using a json criteria" in new WithMongo("test") with Context {
      // Check to see if Right is returned which happens only when no error was returned.
      val initialStory = createEntity(aldgate, "title", "title")

      val story = await(testDAO.create(initialStory)).right.get

      val savedStory = await(testDAO.findOne(Json.obj("uuid" -> story.uuid.get))).right.get.get

      savedStory.storyType must beEqualTo(StoryType.Article)
      savedStory.uuid.get must beEqualTo(story.uuid.get)
    }

    "find entities using a json criteria, page information and sort filter" in new WithMongo("test") with Context {
      // Check to see if Right is returned which happens only when no error was returned.
      await(testDAO.create(createEntity(aldgate, "aldgate", "aldgate")))
      await(testDAO.create(createEntity(stPauls, "stPauls", "stPauls")))
      await(testDAO.create(createEntity(barbican, "barbican", "barbican")))
      await(testDAO.create(createEntity(londonBridge, "londonBridge", "londonBridge")))

      val searchResult = await(testDAO.find(
        criteria = Json.obj("title" -> "aldgate"),
        limit = 2, sortFilter = None
      )).right.get

      searchResult.page must beEqualTo(0)
      searchResult.items.size must beEqualTo(1)
      searchResult.items.head.title must beEqualTo("aldgate")
    }
  }

  "The `update` method" should {
    "update an entity identified by it's primary ID." in new WithMongo("test") with Context {
      // Check to see if Right is returned which happens only when no error was returned.
      val initialStory = createEntity(aldgate, "title", "title")

      val story = await(testDAO.create(initialStory)).right.get

      val storyToUpdate = story.copy(title = "title2")

      await(testDAO.update(story.uuid.get, storyToUpdate))

      val updatedStory = await(testDAO.findOne(Json.obj("uuid" -> story.uuid.get))).right.get.get

      updatedStory.storyType must beEqualTo(StoryType.Article)
      updatedStory.uuid.get must beEqualTo(story.uuid.get)
      updatedStory.title must beEqualTo("title2")
    }
  }

  "The `delete` method" should {
    "remove an entity" in new WithMongo("test") with Context {
      // Check to see if Right is returned which happens only when no error was returned.
      val initialStory = createEntity(aldgate, "title", "title")

      val story = await(testDAO.create(initialStory)).right.get

      val savedStory = await(testDAO.findOne(Json.obj("uuid" -> story.uuid.get))).right.get

      savedStory must not beEmpty

      await(testDAO.delete(story.uuid.get))

      val storyAfterDelete = await(testDAO.findOne(Json.obj("uuid" -> story.uuid.get))).right.get

      storyAfterDelete must beEmpty
    }
  }

  "The `read` method" should {
    "read an entity with uuid" in new WithMongo("test") with Context {
      // Check to see if Right is returned which happens only when no error was returned.
      val initialStory = createEntity(aldgate, "title", "title")

      val story = await(testDAO.create(initialStory)).right.get

      val savedStory = await(testDAO.read(story.uuid.get)).get

      savedStory.storyType must beEqualTo(StoryType.Article)
    }

    "read paginated entities using page number and limit" in new WithMongo("test") with Context {
      await(testDAO.create(createEntity(aldgate, "aldgate", "aldgate")))
      await(testDAO.create(createEntity(stPauls, "stPauls", "stPauls")))
      await(testDAO.create(createEntity(barbican, "barbican", "barbican")))
      await(testDAO.create(createEntity(londonBridge, "londonBridge", "londonBridge")))

      val pagedEntities1 = await(testDAO.read(0, 2)).right.get

      pagedEntities1.page must beEqualTo(0)
      pagedEntities1.total must beEqualTo(4)
      pagedEntities1.items.size must beEqualTo(2)
      pagedEntities1.offset must beEqualTo(0)

      val pagedEntities2 = await(testDAO.read(1, 2)).right.get

      pagedEntities2.page must beEqualTo(1)
      pagedEntities2.total must beEqualTo(4)
      pagedEntities2.items.size must beEqualTo(2)
      pagedEntities2.offset must beEqualTo(2)
    }
  }

  "The `getNear` method" should {
    "should find locations near the coordinates" in new WithMongo("test") with Context {
      await(testDAO.create(createEntity(aldgate, "aldgate", "aldgate")))
      await(testDAO.create(createEntity(stPauls, "stPauls", "stPauls")))
      await(testDAO.create(createEntity(barbican, "barbican", "barbican")))
      await(testDAO.create(createEntity(londonBridge, "londonBridge", "londonBridge")))

      val tateModern = Coordinate(-0.0989392, 51.5081062)

      val nearResults = await(testDAO.nearPoint(tateModern.lon, tateModern.lat))

      nearResults should not be empty

      nearResults.right.get.items(0).coordinate.tuple shouldEqual stPauls
      nearResults.right.get.items(1).coordinate.tuple shouldEqual londonBridge
      nearResults.right.get.items(2).coordinate.tuple shouldEqual barbican
      nearResults.right.get.items(3).coordinate.tuple shouldEqual aldgate
    }
  }

  "The `findAndUpdateByCriteria` method" should {
    "should update and return the updated entity" in new WithMongo("test") with Context {
      // Check to see if Right is returned which happens only when no error was returned.
      val initialStory = createEntity(aldgate, "title", "title")

      val story = await(testDAO.create(initialStory)).right.get

      val savedStory = await(testDAO.read(story.uuid.get)).get

      savedStory.storyType must beEqualTo(StoryType.Article)

      val storyToUpdate = savedStory.copy(updatedOn = LocalDateTime.now().plusDays(1), title = "newTitle")

      val updatedStory = await(testDAO.findAndUpdateByCriteria(Json.obj("uuid" -> story.uuid.get, "updatedOn" -> Json.obj("$lte" -> story.updatedOn)), storyToUpdate))
        .right.get.get

      updatedStory.title must beEqualTo("newTitle")

    }
  }

  private def createEntity(t: (Double, Double), title: String, slug: String): TestEntity = {
    val date = LocalDateTime.now()
    TestEntity(None, date, date, StoryType.Article, title, Seq(), Coordinate(t._1, t._2))
  }

  /**
   * The test context.
   */
  trait Context extends MongoScope {
    self: WithServer =>

    lazy val testDAO = new CRUDTestDAOImpl(reactiveMongoAPI)
  }

}
