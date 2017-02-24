package io.github.shyamsalimkumar.mongodb.shapeless.helpers.decoder

import io.github.shyamsalimkumar.mongodb.shapeless.helpers.{ NoneHandler }
import org.bson.types.ObjectId
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson._
import shapeless._

import scala.annotation.implicitNotFound

@implicitNotFound("Try implementing an implicit BsonDecoder[${A}]")
trait BsonDecoder[A] {
  def decode(o: BsonValue): A
}

object BsonDecoder {
  def apply[A](dec: BsonDecoder[A]): BsonDecoder[A] = dec

  def createDecoder[A](func: BsonValue ⇒ A): BsonDecoder[A] =
    new BsonDecoder[A] {
      override def decode(value: BsonValue): A = func(value)
    }

  implicit def objectIdBsonDecoder: BsonDecoder[ObjectId] =
    createDecoder[ObjectId](a ⇒ a match {
      case x: BsonObjectId ⇒ x.getValue
      //case _               ⇒ null // ToDo throw error if reaches here.
    })

  implicit def stringBsonDecoder: BsonDecoder[String] =
    createDecoder[String](a ⇒ a match {
      case x: BsonString ⇒ x.getValue
      //case _ => null // ToDo throw error if reaches here.
    })

  implicit def intBsonDecoder: BsonDecoder[Int] =
    createDecoder[Int](a ⇒ a match {
      case x: BsonInt32 ⇒ x.getValue
      //case _ => null
    })

  implicit def longBsonDecoder: BsonDecoder[Long] =
    createDecoder[Long](a ⇒ a match {
      case x: BsonInt64 ⇒ x.getValue
      //  case _ => null
    })

  implicit def doubleBsonDecoder: BsonDecoder[Double] =
    createDecoder[Double](a ⇒ a match {
      case x: BsonDouble ⇒ x.getValue
      //    case _ => null
    })

  implicit def booleanBsonDecoder: BsonDecoder[Boolean] =
    createDecoder[Boolean](a ⇒ a match {
      case x: BsonBoolean ⇒ x.getValue
      //      case _ => null
    })
}

