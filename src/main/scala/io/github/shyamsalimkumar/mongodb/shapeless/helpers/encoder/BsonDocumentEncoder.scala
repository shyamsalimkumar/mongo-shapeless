package io.github.shyamsalimkumar.mongodb.shapeless.helpers.encoder

import io.github.shyamsalimkumar.mongodb.shapeless.helpers.BsonFieldNameAdapter
import org.mongodb.scala.bson.BsonDocument
import shapeless._
import shapeless.labelled.FieldType

import scala.annotation.implicitNotFound

@implicitNotFound("Try implementing an implicit BsonDocumentEncoder[${A}]")
trait BsonDocumentEncoder[A] extends BsonEncoder[A] {
  def encode(a: A): BsonDocument
}

object BsonDocumentEncoder {
  def apply[A](enc: BsonDocumentEncoder[A]): BsonDocumentEncoder[A] = enc

  def createDocumentEncoder[A](
    func: A ⇒ BsonDocument
  ): BsonDocumentEncoder[A] =
    new BsonDocumentEncoder[A] {
      override def encode(value: A): BsonDocument = func(value)
    }

  implicit val hNilBsonDocumentEncoder: BsonDocumentEncoder[HNil] =
    createDocumentEncoder(_ ⇒ BsonDocument())

  implicit def hListBsonDocumentEncoder[K <: Symbol, H, T <: HList](
    implicit
    witness:          Witness.Aux[K],
    fieldNameAdapter: BsonFieldNameAdapter,
    hEnc:             Lazy[BsonEncoder[H]],
    tEnc:             BsonDocumentEncoder[T]
  ): BsonDocumentEncoder[FieldType[K, H] :: T] = {
    val fieldName: String = fieldNameAdapter.adapt(witness.value.name)
    createDocumentEncoder {
      case (h :: t) ⇒
        val nullableHead = hEnc.value.encode(h)
        /** The encoded value of `nullableHead` could become `null` if a None value is
         *  received for `h`. This depends on implicit `NoneWriter[A]` provided in scope. By
         *  default, None values are NOT written as `null` to the database.
         */
        val tail = tEnc.encode(t)
        Option(nullableHead) match {
          case Some(head) ⇒
            tail.append(fieldName, head)
          case None ⇒ tail
        }
    }
  }

  implicit val cNilBsonDocumentEncoder: BsonDocumentEncoder[CNil] =
    createDocumentEncoder[CNil](_ ⇒ ???)

  implicit def coproductBsonDocumentEncoder[K <: Symbol, H, T <: Coproduct](
    implicit
    witness:          Witness.Aux[K],
    fieldNameAdapter: BsonFieldNameAdapter,
    hEnc:             Lazy[BsonEncoder[H]],
    tEnc:             BsonDocumentEncoder[T]
  ): BsonDocumentEncoder[H :+: T] =
    createDocumentEncoder {
      case Inl(l) ⇒
        val nullableHead = hEnc.value.encode(l)
        /** The encoded value of `nullableHead` could become `null` if a None value is
         *  received for `h`. This depends on implicit `NoneWriter[A]` provided in scope. By
         *  default, None values are NOT written as `null` to the database.
         */
        val fields = Option(nullableHead) match {
          case Some(head) ⇒
            List(
              (fieldNameAdapter.adapt(witness.value.name), head)
            )
          case None ⇒ List.empty
        }
        BsonDocument(fields)
      case Inr(r) ⇒ tEnc.encode(r)
    }

  implicit def genericBsonDocumentEncoder[A, H <: HList](
    implicit
    generic: LabelledGeneric.Aux[A, H],
    hEnc:    Lazy[BsonDocumentEncoder[H]]
  ): BsonDocumentEncoder[A] =
    createDocumentEncoder { value ⇒
      hEnc.value.encode(generic.to(value))
    }
}
