package io.github.shyamsalimkumar.mongodb

import io.github.shyamsalimkumar.mongodb.shapeless.helpers.decoder.{ BsonDocumentDecoder, DecodeError }
import io.github.shyamsalimkumar.mongodb.shapeless.helpers.{ BsonFieldNameAdapter, NoneHandler, NoneWriter }
import io.github.shyamsalimkumar.mongodb.shapeless.helpers.encoder.BsonDocumentEncoder
import org.mongodb.scala.bson.BsonDocument
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

  implicit class PimpedBsonDocument(document: BsonDocument) {
    def as[T <: BaseDBModel](
      implicit
      fieldNameAdapter: BsonFieldNameAdapter,
      dec:              BsonDocumentDecoder[T]
    ): Either[DecodeError, T] = dec.decode(document)
  }
}
