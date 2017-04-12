package io.github.shyamsalimkumar.mongodb.shapeless.helpers.decoder

import io.github.shyamsalimkumar.mongodb.shapeless.helpers.BsonFieldNameAdapter
import org.mongodb.scala.bson.{ BsonDocument, BsonValue }
import org.mongodb.scala.bson.collection.immutable.Document
//import shapeless.labelled.FieldType
//import shapeless.{HList, ::, HNil, Lazy, Witness}
import shapeless._
import shapeless.labelled._

import scala.annotation.implicitNotFound

@implicitNotFound("Try implementing an implicit BsonDocumentDecoder[${A}]")
trait BsonDocumentDecoder[A] extends BsonDecoder[A] {
  def decode(a: BsonDocument): Either[DecodeError, A]
  override def decode(o: BsonValue): Either[DecodeError, A] = o match {
    case b: BsonDocument ⇒ decode(b)
    case b               ⇒ Left(DecodeError(s"Could not decode $b as BsonDocument"))
  }
  def decode(a: Document): Either[DecodeError, A] = decode(a.asInstanceOf[BsonDocument])
}

object BsonDocumentDecoder {
  def apply[A](dec: BsonDocumentDecoder[A]): BsonDocumentDecoder[A] = dec

  def createDocumentDecoder[A](
    func: BsonDocument ⇒ Either[DecodeError, A]
  ): BsonDocumentDecoder[A] =
    new BsonDocumentDecoder[A] {
      override def decode(value: BsonDocument): Either[DecodeError, A] = func(value)
    }

  implicit val hNilBsonDocumentDecoder: BsonDocumentDecoder[HNil] =
    createDocumentDecoder(_ ⇒ Right(HNil))

  implicit def hListBsonDocumentDecoder[K <: Symbol, H, T <: HList](
    implicit
    witness:          Witness.Aux[K],
    fieldNameAdapter: BsonFieldNameAdapter,
    hDec:             Lazy[BsonDecoder[H]],
    tDec:             BsonDocumentDecoder[T]
  ): BsonDocumentDecoder[FieldType[K, H] :: T] = {
    createDocumentDecoder {
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
      case b ⇒ Left(DecodeError(s"Couldn't decode $b as BsonDocument"))
    }
  }

  implicit val cNilBsonDocumentDecoder: BsonDocumentDecoder[CNil] =
    createDocumentDecoder[CNil](_ ⇒ ???)

  implicit def coproductBsonDocumentDecoder[K <: Symbol, H, T <: Coproduct](
    implicit
    witness:          Witness.Aux[K],
    fieldNameAdapter: BsonFieldNameAdapter,
    hDec:             Lazy[BsonDecoder[H]],
    tDec:             BsonDocumentDecoder[T]
  ): BsonDocumentDecoder[H :+: T] =
    createDocumentDecoder {
      case b: BsonDocument ⇒
        val key = fieldNameAdapter.adapt(witness.value.name)
        hDec.value.decode(b.get(key)) match {
          case Right(head) ⇒
            //            Right(Inr(head))
            tDec.decode(b) match {
              case Right(tail) ⇒
                //                Right(head :+: tail)
                //                Right(tail)
                null
              case Left(l) ⇒ Left(l)
            }
          case Left(e) ⇒ Left(e)
        }
      case b ⇒ Left(DecodeError(s"Couldn't decode $b as BsonDocument"))
    }

  implicit def genericBsonDocumentDecoder[A, H <: HList](
    implicit
    generic: LabelledGeneric.Aux[A, H],
    hDec:    Lazy[BsonDocumentDecoder[H]]
  ): BsonDocumentDecoder[A] =
    createDocumentDecoder[A] {
      case b: BsonDocument ⇒
        hDec.value.decode(b) match {
          case Right(head) ⇒ Right(generic.from(head))
          case Left(e)     ⇒ Left(e)
        }
      case b ⇒ Left(DecodeError(s"Couldn't decode $b as BsonDocument"))
    }
}
