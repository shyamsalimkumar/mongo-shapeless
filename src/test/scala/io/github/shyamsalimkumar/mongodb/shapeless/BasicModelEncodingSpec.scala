package io.github.shyamsalimkumar.mongodb.shapeless

import org.bson.types.ObjectId
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{ BsonArray, BsonBoolean, BsonDocument, BsonNull, BsonNumber, BsonObjectId, BsonString }
import org.scalactic.Constraint
import io.github.shyamsalimkumar.mongodb.shapeless.helpers.{ BsonFieldNameAdapter, NoneHandler }
import io.github.shyamsalimkumar.mongodb.shapeless.models.{ ModelC, ModelD, NestModelA, NestModelB }
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{ FlatSpec, Matchers }

//class BasicModelEncodingSpec extends FlatSpec with Matchers with TypeCheckedTripleEquals {
//  implicit val bsonConstraint = new Constraint[Document, BsonDocument] {
//    override def areEqual(a: Document, b: BsonDocument): Boolean = {
//      val aKeySet = a.keySet
//      val aNumKeys = aKeySet.size
//      val bNumKeys = b.keySet().size()
//      val keySizeCheck = aNumKeys == bNumKeys
//      if (!keySizeCheck) {
//        println(s"Key sizes are different. $aNumKeys != $bNumKeys")
//      }
//      keySizeCheck && aKeySet.foldLeft(true) {
//        case (r, s) ⇒
//          val aValue = a.get(s)
//          val bValue = b.get(s)
//          val containsCheck = aValue.contains(bValue)
//          if (!containsCheck) {
//            println(s"Key '$s' did not match")
//            println(aValue)
//            println(bValue)
//          }
//          r && containsCheck
//      }
//    }
//  }
//
//  it should "convert simple models (only supported primitive properties)" in {
//    val model = NestModelB("string", boolValue = true, 10, 10, 10, List("string item"), Option("string item 2"), Map("key" → "asdf"))
//    val document = model.toDocument
//    val expectedDocument = Document(List(
//      ("string_value", BsonString("string")),
//      ("bool_value", BsonBoolean(true)),
//      ("int_value", BsonNumber(10)),
//      ("long_value", BsonNumber(10L)),
//      ("double_value", BsonNumber(10D)),
//      ("seq_value", BsonArray(BsonString("string item"))),
//      ("opt_value", BsonString("string item 2")),
//      ("map_value", BsonDocument(List(
//        ("key", BsonString("asdf"))
//      )))
//    ))
//    assert(document === expectedDocument)
//  }
//
//  it should "convert model with nested elements" in {
//    val id = new ObjectId()
//    val nestedModel = NestModelB("string", boolValue = true, 10, 10, 10, List("string item"), Option("string item 2"), Map("key" → "asdf"))
//    val model = NestModelA(id, "string 1", 20, 20, 20, List("string item 1"), Option("string item 21"), Map("key1" → "asdf1"), nestedModel, List(nestedModel), Map("key1" → nestedModel), Option(nestedModel))
//    val nestedDocument = BsonDocument(List(
//      ("string_value", BsonString("string")),
//      ("bool_value", BsonBoolean(true)),
//      ("int_value", BsonNumber(10)),
//      ("long_value", BsonNumber(10L)),
//      ("double_value", BsonNumber(10D)),
//      ("seq_value", BsonArray(BsonString("string item"))),
//      ("opt_value", BsonString("string item 2")),
//      ("map_value", BsonDocument(List(
//        ("key", BsonString("asdf"))
//      )))
//    ))
//    val document = model.toDocument
//    val expectedDocument = Document(List(
//      ("_id", BsonObjectId(id)),
//      ("string_value", BsonString("string 1")),
//      ("int_value", BsonNumber(20)),
//      ("long_value", BsonNumber(20L)),
//      ("double_value", BsonNumber(20D)),
//      ("seq_value", BsonArray(BsonString("string item 1"))),
//      ("opt_value", BsonString("string item 21")),
//      ("map_value", BsonDocument(List(
//        ("key1", BsonString("asdf1"))
//      ))),
//      ("nest_model_b", nestedDocument),
//      ("seq_value2", BsonArray(nestedDocument)),
//      ("map_value2", BsonDocument(List(
//        ("key1", nestedDocument)
//      ))),
//      ("opt_value2", nestedDocument)
//    ))
//
//    assert(document === expectedDocument)
//  }
//
//  it should "convert model with None to document without the field" in {
//    val model = NestModelB("string", boolValue = true, 10, 10, 10, List("string item"), None, Map("key" → "asdf"))
//    val document = model.toDocument
//    val expectedDocument = Document(List(
//      ("string_value", BsonString("string")),
//      ("bool_value", BsonBoolean(true)),
//      ("int_value", BsonNumber(10)),
//      ("long_value", BsonNumber(10L)),
//      ("double_value", BsonNumber(10D)),
//      ("seq_value", BsonArray(BsonString("string item"))),
//      ("map_value", BsonDocument(List(
//        ("key", BsonString("asdf"))
//      )))
//    ))
//    assert(document === expectedDocument)
//  }
//
//  it should "convert model with None to document with the field with value as null" in {
//    val model = NestModelB("string", boolValue = true, 10, 10, 10, List("string item"), None, Map("key" → "asdf"))
//    implicit val explicitNullNoneHandler = NoneHandler.explicitNullNoneWriter
//    val document = model.toDocument
//    val expectedDocument = Document(List(
//      ("string_value", BsonString("string")),
//      ("bool_value", BsonBoolean(true)),
//      ("int_value", BsonNumber(10)),
//      ("long_value", BsonNumber(10L)),
//      ("double_value", BsonNumber(10D)),
//      ("seq_value", BsonArray(BsonString("string item"))),
//      ("opt_value", BsonNull()),
//      ("map_value", BsonDocument(List(
//        ("key", BsonString("asdf"))
//      )))
//    ))
//    assert(document === expectedDocument)
//  }
//
//  it should "only convert 'id' field name to '_id'" in {
//    val id = new ObjectId()
//    val nestedModel = NestModelB("string", boolValue = true, 10, 10, 10, List("string item"), Option("string item 2"), Map("key" → "asdf"))
//    val model = NestModelA(id, "string 1", 20, 20, 20, List("string item 1"), Option("string item 21"), Map("key1" → "asdf1"), nestedModel, List(nestedModel), Map("key1" → nestedModel), Option(nestedModel))
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
//    implicit val idToPrimaryKeyBsonFieldNameAdapter = BsonFieldNameAdapter.idToPrimaryKeyAdapter
//    val document = model.toDocument
//    val expectedDocument = Document(List(
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
//
//    assert(document === expectedDocument)
//  }
//
//  it should "allow embedding of documents into the model" in {
//    val id = new ObjectId()
//    val modelC = ModelC(id, BsonDocument())
//    val expectedDocC = Document(List(
//      ("_id", BsonObjectId(id)),
//      ("doc", BsonDocument())
//    ))
//
//    assert(modelC.toDocument == expectedDocC)
//
//    val modelD = ModelD(id, Document())
//    val expectedDocD = Document(List(
//      ("_id", BsonObjectId(id)),
//      ("doc", BsonDocument())
//    ))
//
//    assert(modelD.toDocument == expectedDocD)
//  }
//}
