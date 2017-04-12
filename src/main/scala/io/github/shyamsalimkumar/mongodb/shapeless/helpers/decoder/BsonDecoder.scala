package io.github.shyamsalimkumar.mongodb.shapeless.helpers.decoder

//import io.github.shyamsalimkumar.mongodb.shapeless.helpers.NoneHandler
import io.github.shyamsalimkumar.mongodb.shapeless.helpers.BsonFieldNameAdapter
import io.github.shyamsalimkumar.mongodb.shapeless.helpers.decoder.BsonDocumentDecoder.createDocumentDecoder
import org.bson.types.ObjectId
import shapeless.labelled.{ FieldType, field }
import shapeless.{ ::, HList, HNil, LabelledGeneric, Lazy, Witness }
//import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson._
//import shapeless._

import scala.annotation.implicitNotFound
import scala.collection.JavaConverters._
import scala.collection.mutable

@implicitNotFound("Try implementing an implicit BsonDecoder[${A}]")
trait BsonDecoder[A] {
  def decode(o: BsonValue): Either[DecodeError, A]
}

object BsonDecoder {
  def apply[A](dec: BsonDecoder[A]): BsonDecoder[A] = dec

  def createDecoder[A](func: BsonValue ⇒ Either[DecodeError, A]): BsonDecoder[A] =
    new BsonDecoder[A] {
      override def decode(value: BsonValue): Either[DecodeError, A] = func(value)
    }

  implicit def objectIdBsonDecoder: BsonDecoder[ObjectId] =
    createDecoder[ObjectId] {
      case x: BsonObjectId ⇒ Right(x.getValue)
      case x               ⇒ Left(DecodeError(s"Could not parse $x as ObjectId"))
    }

  implicit def stringBsonDecoder: BsonDecoder[String] =
    createDecoder[String] {
      case x: BsonString ⇒ Right(x.getValue)
      case x             ⇒ Left(DecodeError(s"Could not parse $x as String"))
    }

  implicit def intBsonDecoder: BsonDecoder[Int] =
    createDecoder[Int] {
      case x: BsonInt32 ⇒ Right(x.getValue)
      case x            ⇒ Left(DecodeError(s"Could not parse $x as Int"))
    }

  implicit def longBsonDecoder: BsonDecoder[Long] =
    createDecoder[Long] {
      case x: BsonInt64 ⇒ Right(x.getValue)
      case x            ⇒ Left(DecodeError(s"Could not parse $x as Long"))
    }

  implicit def doubleBsonDecoder: BsonDecoder[Double] =
    createDecoder[Double] {
      case x: BsonDouble ⇒ Right(x.getValue)
      case x             ⇒ Left(DecodeError(s"Could not parse $x as Double"))
    }

  implicit def booleanBsonDecoder: BsonDecoder[Boolean] =
    createDecoder[Boolean] {
      case x: BsonBoolean ⇒ Right(x.getValue)
      case x              ⇒ Left(DecodeError(s"Could not parse $x as Boolean"))
    }

  implicit def bsonDocumentBsonDecoder: BsonDecoder[BsonDocument] =
    createDecoder[BsonDocument] {
      case x: BsonDocument ⇒ Right(x)
      case x               ⇒ Left(DecodeError(s"Could not parse $x as BsonDocument"))
    }

  implicit def documentBsonDecoder: BsonDecoder[Document] =
    createDecoder[Document] {
      case x: BsonDocument ⇒ Right(x)
      case x               ⇒ Left(DecodeError(s"Could not parse $x as Document"))
    }

  implicit def optBsonDecoder[T](
    implicit
    decoder: BsonDecoder[T]
  ): BsonDecoder[Option[T]] =
    createDecoder[Option[T]]{
      a ⇒
        val b = Option(a)
        b match {
          case Some(c) ⇒ decoder.decode(c) match {
            case Right(r) ⇒ Right(Option(r))
            case Left(l)  ⇒ Left(l)
          }
          case None ⇒ Right(None)
        }
    }

  implicit def seqBsonDecoder[T](
    implicit
    decoder: BsonDecoder[T]
  ): BsonDecoder[Seq[T]] =
    createDecoder[Seq[T]] {
      case arr: BsonArray ⇒
        val listBuffer = mutable.ListBuffer.empty[T]
        var decodeError: Option[DecodeError] = None
        val arrValueIterator = arr.getValues.asScala.toIterator
        while (arrValueIterator.hasNext && decodeError.isEmpty) {
          val next = arrValueIterator.next()
          decoder.decode(next) match {
            case Right(r) ⇒ listBuffer += r
            case Left(l) ⇒
              decodeError = Option(l)
          }
        }

        decodeError match {
          case Some(e) ⇒ Left(e)
          case None    ⇒ Right(listBuffer)
        }
      case o ⇒ Left(DecodeError(s"Could not parse $o as BsonArray"))
    }

  implicit def mapBsonDecoder[T](
    implicit
    decoder: BsonDecoder[T]
  ): BsonDecoder[Map[String, T]] =
    createDecoder[Map[String, T]] {
      case b: BsonDocument ⇒
        val mutableMap = mutable.Map.empty[String, T]
        var decodeError: Option[DecodeError] = None
        val keySetIterator = b.keySet().asScala.toIterator
        while (keySetIterator.hasNext && decodeError.isEmpty) {
          val key = keySetIterator.next()
          decoder.decode(b.get(key)) match {
            case Right(r) ⇒ mutableMap += ((key, r))
            case Left(l)  ⇒ decodeError = Option(l)
          }
        }

        decodeError match {
          case Some(e) ⇒ Left(e)
          case None    ⇒ Right(mutableMap.toMap)
        }
      case b ⇒ Left(DecodeError(s"Couldn't parse $b as Map"))
    }

  implicit val hNilBsonDecoder: BsonDecoder[HNil] =
    createDecoder(_ ⇒ Right(HNil))

  implicit def hListBsonDecoder[K <: Symbol, H, T <: HList](
    implicit
    witness:          Witness.Aux[K],
    fieldNameAdapter: BsonFieldNameAdapter,
    hDec:             Lazy[BsonDecoder[H]],
    tDec:             BsonDocumentDecoder[T]
  ): BsonDecoder[FieldType[K, H] :: T] =
    createDecoder {
      case b: BsonDocument ⇒
        val key = fieldNameAdapter.adapt(witness.value.name)
        hDec.value.decode(b.get(key)) match {
          case Right(head) ⇒
            tDec.decode(b) match {
              case Right(r) ⇒ Right(field[K](head) :: r)
              case Left(e)  ⇒ Left(e)
            }
          case Left(e) ⇒ Left(e)
        }
      case x ⇒ Left(DecodeError(s"Could not parse $x as BsonValue"))
    }

  // Are the HNil/CNil and HList/CList cases required? If yes, why?
  implicit def genericBsonDecoder[A, H <: HList](
    generic: LabelledGeneric.Aux[A, H],
    hDec:    Lazy[BsonDocumentDecoder[H]]
  ): BsonDecoder[A] =
    createDecoder[A] {
      case b: BsonDocument ⇒
        hDec.value.decode(b) match {
          case Right(r) ⇒ Right(generic.from(r))
          case Left(l)  ⇒ Left(l)
        }
      case b ⇒ Left(DecodeError(s"Couldn't parse $b as BsonDocument"))
    }
}

case class DecodeError(msg: String)