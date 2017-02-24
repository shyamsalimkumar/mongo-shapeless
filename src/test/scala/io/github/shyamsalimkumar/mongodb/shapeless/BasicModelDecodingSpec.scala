package io.github.shyamsalimkumar.mongodb.shapeless

import org.bson.types.ObjectId
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson._
import org.scalactic.Constraint
import io.github.shyamsalimkumar.mongodb.shapeless.helpers.{ BsonFieldNameAdapter, NoneHandler }
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{ FlatSpec, Matchers }
import org.scalatest._

class BasicModelDecodingSpec extends FlatSpec with Matchers with TypeCheckedTripleEquals {
  it should "convert to simple models (only supported primitive properties)" in pending

  it should "throw error on receiving invalid data" in pending

  it should "convert to model with nested elements" in pending

  it should "convert to model with None if field is missing" in pending

  it should "convert to model with None if field value is null (and it is of Option[T] type)" in pending

  it should "convert to model with '_id' mapped to id" in pending

  it should "allow retrieval of embedded documents as such" in pending
}
