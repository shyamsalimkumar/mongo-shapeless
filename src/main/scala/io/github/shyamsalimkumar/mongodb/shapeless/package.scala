package io.github.shyamsalimkumar.mongodb

import io.github.shyamsalimkumar.mongodb.shapeless.helpers.{ BsonFieldNameAdapter, NoneHandler, NoneWriter }
import io.github.shyamsalimkumar.mongodb.shapeless.helpers.encoder.BsonDocumentEncoder
import io.github.shyamsalimkumar.mongodb.shapeless.helpers.decoder.BsonDocumentDecoder
import org.mongodb.scala.bson.collection.immutable.Document

package object shapeless {

  implicit class PimpedBaseDBModel[T <: BaseDBModel, U <: NoneHandler](dbModel: T) {
    def toDocument(
      implicit
      fieldNameAdapter: BsonFieldNameAdapter,
      noneWriter:       NoneWriter[U],
      enc:              BsonDocumentEncoder[T]
    ): Document = enc.encode(dbModel)
  }

  implicit class PimpedDocument[T <: BaseDBModel](document: Document) {
    def as[T](
      implicit
      dec: BsonDocumentDecoder[T]
    ): T = dec.decode(document)
  }
}
