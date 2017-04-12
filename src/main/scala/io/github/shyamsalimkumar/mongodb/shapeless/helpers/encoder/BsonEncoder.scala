package io.github.shyamsalimkumar.mongodb.shapeless.helpers.encoder

import io.github.shyamsalimkumar.mongodb.shapeless.helpers.{ NoneHandler, NoneWriter }
import org.bson.types.ObjectId
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{ BsonDocument, BsonNull, BsonTransformer, BsonValue }
import shapeless._

import scala.annotation.implicitNotFound

@implicitNotFound("Try implementing an implicit BsonEncoder[${A}]")
trait BsonEncoder[A] {
  def encode(a: A): BsonValue
}

object BsonEncoder {
  def apply[A](enc: BsonEncoder[A]): BsonEncoder[A] = enc

  def createEncoder[A](func: A ⇒ BsonValue): BsonEncoder[A] =
    new BsonEncoder[A] {
      override def encode(value: A): BsonValue = func(value)
    }

  implicit def objectIdBsonEncoder(implicit transformer: BsonTransformer[ObjectId]): BsonEncoder[ObjectId] =
    createEncoder[ObjectId](a ⇒ transformer(a))
  implicit def stringBsonEncoder(implicit transformer: BsonTransformer[String]): BsonEncoder[String] =
    createEncoder[String](a ⇒ transformer(a))
  implicit def intBsonEncoder(implicit transformer: BsonTransformer[Int]): BsonEncoder[Int] =
    createEncoder[Int](a ⇒ transformer(a))
  implicit def longBsonEncoder(implicit transformer: BsonTransformer[Long]): BsonEncoder[Long] =
    createEncoder[Long](a ⇒ transformer(a))
  implicit def doubleBsonEncoder(implicit transformer: BsonTransformer[Double]): BsonEncoder[Double] =
    createEncoder[Double](a ⇒ transformer(a))
  implicit def booleanBsonEncoder(implicit transformer: BsonTransformer[Boolean]): BsonEncoder[Boolean] =
    createEncoder[Boolean](a ⇒ transformer(a))

  /** This allows a `BsonDocument` to be used as such in a case class. It should be used in favor of
   *  `Map[String, Any]`
   */
  implicit def bsonDocumentBsonEncoder(implicit transformer: BsonTransformer[BsonDocument]): BsonEncoder[BsonDocument] = createEncoder(a ⇒ transformer(a))
  /** This allows a `immutable.Document` to be used as such in a case class. It should be used in
   *  favor of `Map[String, Any]`
   */
  implicit def documentBsonEncoder(implicit transformer: BsonTransformer[Document]): BsonEncoder[Document] = createEncoder(a ⇒ transformer(a))

  implicit def optBsonEncoder[T, U <: NoneHandler](
    implicit
    noneWriter: NoneWriter[U],
    encoder:    BsonEncoder[T]
  ): BsonEncoder[Option[T]] =
    createEncoder[Option[T]](a ⇒ {
      val encodedValue = a.map(encoder.encode)
      if (noneWriter.shouldWrite()) {
        encodedValue.getOrElse(BsonNull())
      } else {
        encodedValue.orNull
      }
    })
  implicit def seqBsonEncoder[T](
    implicit
    wrapperTransformer: BsonTransformer[Seq[BsonValue]],
    encoder:            BsonEncoder[T]
  ): BsonEncoder[Seq[T]] =
    createEncoder[Seq[T]](a ⇒ wrapperTransformer(a.map(encoder.encode)))
  implicit def mapBsonEncoder[T](
    implicit
    wrapperTransformer: BsonTransformer[Seq[(String, BsonValue)]],
    encoder:            BsonEncoder[T]
  ): BsonEncoder[Map[String, T]] =
    createEncoder[Map[String, T]](a ⇒ wrapperTransformer(a.mapValues(encoder.encode).toSeq))

  // Are the HNil/CNil and HList/CList cases required? If yes, why?
  implicit def genericBsonEncoder[A, H <: HList](
    implicit
    generic: LabelledGeneric.Aux[A, H],
    hEnc:    Lazy[BsonDocumentEncoder[H]]
  ): BsonEncoder[A] =
    createEncoder { value ⇒
      hEnc.value.encode(generic.to(value))
    }
}
