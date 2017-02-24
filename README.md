##MongoDB Shapeless
My trial at using Shapeless with MongoDB

###Supported Types
Currently it supports the following types as base properties for case classes.

1. `String`
1. `Int`
1. `Long`
1. `Double`
1. `Boolean`
1. `org.bson.types.ObjectId`
1. `Seq[A]`, where `A` is any of the first six
1. `Map[String, A]`, where `A` is any of the first six
1. `Option[A]`, where `A` is any of the first six
1. `org.mongodb.scala.bson.collection.immutable.Document`
1. `org.mongodb.scala.bson.BsonDocument` 

###Usage

```scala
import io.github.shyamsalimkumar.mongodb.shapeless._

case class User(id: String, name: String, age: Int) extends BaseDBModel

val user = User("user-001", "User 01", 50)
val userDocument = user.toDocument

// Document((age,BsonInt32{value=50}), (name,BsonString{value='User 01'}), (_id,BsonString{value='user-001'}))
```

#####Note

`Any` is not supported so instead of using `Map[String, Any]` use either `Document` 
or `BsonDocument`. If you use something like `Map[String, Any]` it will result in a 
compile error looking like the following

```bash
Try implementing an implicit BsonDocumentEncoder[io.github.shyamsalimkumar.mongodb.shapeless.models.ModelD]
```

This error basically means that something in the `ModelD` model is not supported