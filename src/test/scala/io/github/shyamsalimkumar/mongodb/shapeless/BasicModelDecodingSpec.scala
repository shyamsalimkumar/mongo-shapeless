package io.github.shyamsalimkumar.mongodb.shapeless

import io.github.shyamsalimkumar.mongodb.shapeless.helpers.decoder.{ BsonDecoder, BsonDocumentDecoder, DecodeError }
import io.github.shyamsalimkumar.mongodb.shapeless.models.{ ModelC, NestModelA, NestModelADupe, NestModelB }
import org.mongodb.scala.bson._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{ FlatSpec, Matchers }
import shapeless._
import shapeless.labelled._

class BasicModelDecodingSpec extends FlatSpec with Matchers with TypeCheckedTripleEquals {
  it should "convert to simple models (only supported primitive properties)" in {

    val doc = BsonDocument(List(
      ("string_value", BsonString("abcd")),
      ("bool_value", BsonBoolean(true)),
      ("int_value", BsonInt32(10)),
      ("long_value", BsonInt64(10000L)),
      ("double_value", BsonDouble(1000000.0)),
      ("opt_value", BsonString("asdf")),
      ("seq_value", BsonArray("abcdabcd")),
      ("map_value", BsonDocument(List(
        ("v1", BsonString("abcdabcd"))
      )))
    ))

    val decodeErrorOrA1 = doc.as[NestModelB]
    assert(decodeErrorOrA1.isRight)
    assert(decodeErrorOrA1.right.get.stringValue == "abcd")
    assert(decodeErrorOrA1.right.get.boolValue)
    assert(decodeErrorOrA1.right.get.intValue == 10)
    assert(decodeErrorOrA1.right.get.longValue == 10000L)
    assert(decodeErrorOrA1.right.get.doubleValue == 1000000.0)
    assert(decodeErrorOrA1.right.get.seqValue == Seq("abcdabcd"))
    assert(decodeErrorOrA1.right.get.optValue == Option("asdf"))
    assert(decodeErrorOrA1.right.get.mapValue == Map("v1" → "abcdabcd"))

  }

  it should "throw error on receiving invalid data" in {
    {
      val doc = BsonDocument(List(
        ("string_value", BsonString("abcd")),
        ("bool_value", BsonString("asdasd")),
        ("int_value", BsonInt32(10)),
        ("long_value", BsonInt64(10000L)),
        ("double_value", BsonDouble(1000000.0)),
        ("opt_value", BsonString("asdf")),
        ("seq_value", BsonArray("abcdabcd")),
        ("map_value", BsonDocument(List(
          ("v1", BsonString("abcdabcd"))
        )))
      ))

      val decodeErrorOrA1 = doc.as[NestModelB]
      assert(decodeErrorOrA1.isLeft)
      assert(decodeErrorOrA1 == Left(DecodeError("Could not parse BsonString{value='asdasd'} as Boolean")))
    }
  }

  it should "convert to model with nested elements" in {
    val id = new ObjectId()
    val nestedDocument = BsonDocument(List(
      ("string_value", BsonString("string")),
      ("bool_value", BsonBoolean(true)),
      ("int_value", BsonNumber(10)),
      ("long_value", BsonNumber(10L)),
      ("double_value", BsonNumber(10D)),
      ("seq_value", BsonArray(BsonString("string item"))),
      ("opt_value", BsonString("string item 2")),
      ("map_value", BsonDocument(List(
        ("key", BsonString("asdf"))
      )))
    ))
    val doc = BsonDocument(List(
      ("_id", BsonObjectId(id)),
      ("bool_value", BsonBoolean(true)),
      ("string_value", BsonString("string 1")),
      ("int_value", BsonNumber(20)),
      ("long_value", BsonNumber(20L)),
      ("double_value", BsonNumber(20D)),
      ("seq_value", BsonArray(BsonString("string item 1"))),
      ("opt_value", BsonString("string item 21")),
      ("map_value", BsonDocument(List(
        ("key1", BsonString("asdf1"))
      ))),
      ("nest_model_b", nestedDocument),
      ("seq_value2", BsonArray(nestedDocument)),
      ("map_value2", BsonDocument(List(
        ("key1", nestedDocument)
      ))),
      ("opt_value2", nestedDocument)
    ))

    val decodeErrorOrA = doc.as[NestModelA]
    assert(decodeErrorOrA.isRight)
    assert(decodeErrorOrA.right.get.id == id)
    assert(decodeErrorOrA.right.get.boolValue)
    assert(decodeErrorOrA.right.get.stringValue == "string 1")
    assert(decodeErrorOrA.right.get.intValue == 20)
    assert(decodeErrorOrA.right.get.longValue == 20L)
    assert(decodeErrorOrA.right.get.doubleValue == 20D)
    assert(decodeErrorOrA.right.get.seqValue == Seq("string item 1"))
    assert(decodeErrorOrA.right.get.optValue == Option("string item 21"))
    assert(decodeErrorOrA.right.get.mapValue == Map("key1" → "asdf1"))

    val nestedModel = NestModelB("string", boolValue = true, 10, 10, 10, List("string item"), Option("string item 2"), Map("key" → "asdf"))
    val model = NestModelADupe(id, boolValue = true, "string 1", 20, 20, 20, List("string item 1"), Option("string item 21"), Map("key1" → "asdf1"), nestedModel, List(nestedModel), Map("key1" → nestedModel), Option(nestedModel))

    println("=" * 50)
    println(implicitly[LabelledGeneric[NestModelADupe]].to(model))
    println(implicitly[BsonDecoder[LabelledGeneric[HNil]]])

    //    implicitly[BsonDocumentDecoder[NestModelA]]
    //    implicitly[BsonDecoder[Map[String, Map[String, HNil]]]]
    //    implicitly[BsonDecoder[String :: HNil]]
  }

  it should "convert to model with None if field value is null (and it is of Option[T] type)" in {
    val doc = BsonDocument(List(
      ("string_value", BsonString("abcd")),
      ("bool_value", BsonBoolean(true)),
      ("int_value", BsonInt32(10)),
      ("long_value", BsonInt64(10000L)),
      ("double_value", BsonDouble(1000000.0)),
      ("seq_value", BsonArray("abcdabcd")),
      ("map_value", BsonDocument(List(
        ("v1", BsonString("abcdabcd"))
      )))
    ))

    val decodeErrorOrA1 = doc.as[NestModelB]
    assert(decodeErrorOrA1.isRight)
    assert(decodeErrorOrA1.right.get.stringValue == "abcd")
    assert(decodeErrorOrA1.right.get.boolValue)
    assert(decodeErrorOrA1.right.get.intValue == 10)
    assert(decodeErrorOrA1.right.get.longValue == 10000L)
    assert(decodeErrorOrA1.right.get.doubleValue == 1000000.0)
    assert(decodeErrorOrA1.right.get.seqValue == Seq("abcdabcd"))
    assert(decodeErrorOrA1.right.get.optValue.isEmpty)
    assert(decodeErrorOrA1.right.get.mapValue == Map("v1" → "abcdabcd"))

  }

  //  it should "convert to model with '_id' mapped to id" in {
  //    val id = new ObjectId()
  //    val nestedDocument = BsonDocument(List(
  //      ("stringValue", BsonString("string")),
  //      ("boolValue", BsonBoolean(true)),
  //      ("intValue", BsonNumber(10)),
  //      ("longValue", BsonNumber(10L)),
  //      ("doubleValue", BsonNumber(10D)),
  //      ("seqValue", BsonArray(BsonString("string item"))),
  //      ("optValue", BsonString("string item 2")),
  //      ("mapValue", BsonDocument(List(
  //        ("key", BsonString("asdf"))
  //      )))
  //    ))
  //    val doc = Document(List(
  //      ("_id", BsonObjectId(id)),
  //      ("stringValue", BsonString("string 1")),
  //      ("intValue", BsonNumber(20)),
  //      ("longValue", BsonNumber(20L)),
  //      ("doubleValue", BsonNumber(20D)),
  //      ("seqValue", BsonArray(BsonString("string item 1"))),
  //      ("optValue", BsonString("string item 21")),
  //      ("mapValue", BsonDocument(List(
  //        ("key1", BsonString("asdf1"))
  //      ))),
  //      ("nestModelB", nestedDocument),
  //      ("seqValue2", BsonArray(nestedDocument)),
  //      ("mapValue2", BsonDocument(List(
  //        ("key1", nestedDocument)
  //      ))),
  //      ("optValue2", nestedDocument)
  //    ))
  //    implicit val idToPrimaryKeyBsonFieldNameAdapter = BsonFieldNameAdapter.idToPrimaryKeyAdapter
  //
  //    val decodeErrorOrA = doc.as[NestModelA]
  //    assert(decodeErrorOrA.isRight)
  //    // todo: Add more assertions
  //  }

  it should "allow retrieval of embedded documents as such" in {
    val id = new ObjectId()
    val doc = BsonDocument(List(
      ("_id", BsonObjectId(id)),
      ("doc", BsonDocument())
    ))

    val decodeErrorOrC = doc.as[ModelC]
    assert(decodeErrorOrC.isRight)
    assert(decodeErrorOrC.right.get.id == id)
    assert(decodeErrorOrC.right.get.doc == BsonDocument())
  }
}
