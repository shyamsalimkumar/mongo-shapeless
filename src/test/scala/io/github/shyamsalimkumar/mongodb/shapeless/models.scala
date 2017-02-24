package io.github.shyamsalimkumar.mongodb.shapeless

import org.bson.types.ObjectId
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.collection.immutable.Document

object models {

  case class NestModelA(
    id:          ObjectId,
    stringValue: String,
    intValue:    Int,
    longValue:   Long,
    doubleValue: Double,
    seqValue:    Seq[String],
    optValue:    Option[String],
    mapValue:    Map[String, String],
    nestModelB:  NestModelB,
    seqValue2:   Seq[NestModelB],
    mapValue2:   Map[String, NestModelB],
    optValue2:   Option[NestModelB]
  ) extends BaseDBModel

  case class NestModelB(
    stringValue: String,
    intValue:    Int,
    longValue:   Long,
    doubleValue: Double,
    seqValue:    Seq[String],
    optValue:    Option[String],
    mapValue:    Map[String, String]
  ) extends BaseDBModel

  case class ModelC(
    id:  ObjectId,
    doc: BsonDocument
  ) extends BaseDBModel

  case class ModelD(
    id:  ObjectId,
    doc: Document
  ) extends BaseDBModel

}
