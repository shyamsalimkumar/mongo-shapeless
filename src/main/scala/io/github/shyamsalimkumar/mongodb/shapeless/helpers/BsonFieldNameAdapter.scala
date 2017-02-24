package io.github.shyamsalimkumar.mongodb.shapeless.helpers

import scala.annotation.implicitNotFound

@implicitNotFound("Try implementing an implicit BsonFieldNameAdapter")
trait BsonFieldNameAdapter {
  def adapt(fieldName: String): String
}

object BsonFieldNameAdapter {
  val camelToSnakeCaseAdapter = new BsonFieldNameAdapter {
    override def adapt(fieldName: String) =
      fieldName
        .foldLeft("")((r, x) ⇒ r + (if (x == x.toLower) x.toString else "_" + x.toLower.toString))
  }

  val snakeToCamelCaseAdapter = new BsonFieldNameAdapter {
    override def adapt(fieldName: String): String = fieldName.split('_').toList match {
      case head :: tail ⇒ head + tail.map(_.capitalize)
      case Nil          ⇒ ""
    }
  }

  val idToPrimaryKeyAdapter = new BsonFieldNameAdapter {
    override def adapt(fieldName: String) = if (fieldName == "id") "_id" else fieldName
  }

  // Default adapter
  implicit val id2PKeyNCam2SnakeAdapter = new BsonFieldNameAdapter {
    override def adapt(fieldName: String) = camelToSnakeCaseAdapter.adapt(idToPrimaryKeyAdapter.adapt(fieldName))
  }
}
