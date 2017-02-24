package io.github.shyamsalimkumar.mongodb.shapeless.helpers.decoder

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.collection.immutable.Document
import shapeless._
import shapeless.labelled.FieldType

import scala.annotation.implicitNotFound

@implicitNotFound("Try implementing an implicit BsonDocumentDecoder[${A}]")
trait BsonDocumentDecoder[A] extends BsonDecoder[A] {
  def decode(a: BsonDocument): A
  def decode(a: Document): A = decode(a.asInstanceOf[BsonDocument])
}

object BsonDocumentDecoder {
  def apply[A](dec: BsonDocumentDecoder[A]): BsonDocumentDecoder[A] = dec

  def createDocumentDecoder[A](
    func: BsonDocument ⇒ A
  ): BsonDocumentDecoder[A] =
    new BsonDocumentDecoder[A] {
      override def decode(value: BsonDocument): A = func(value)
    }

  implicit val hNilBsonDocumentDecoder: BsonDocumentDecoder[HNil] =
    createDocumentDecoder(_ ⇒ HNil)

  implicit def hListBsonDocumentDecoder[K <: Symbol, H, T <: HList](
    implicit
    witness: Witness.Aux[K],
    hDec:    Lazy[BsonDecoder[H]],
    tDec:    BsonDocumentDecoder[T]
  ): BsonDocumentDecoder[FieldType[K, H] :: T] = {
    createDocumentDecoder {
      case (h :: t) ⇒
        println(h)
        println(t)
        println(witness.value.name)
        hDec.value.decode(h)
        tDec.decode(t)
    }
  }

  implicit val cNilBsonDocumentDecoder: BsonDocumentDecoder[CNil] =
    createDocumentDecoder[CNil](_ ⇒ ???)

  implicit def coproductBsonDocumentDecoder[K <: Symbol, H, T <: Coproduct](
    implicit
    witness: Witness.Aux[K],
    hDec:    Lazy[BsonDecoder[H]],
    tDec:    BsonDocumentDecoder[T]
  ): BsonDocumentDecoder[H :+: T] =
    createDocumentDecoder {
      case Inl(l) ⇒
        null
      case Inr(r) ⇒
        null
    }
}
