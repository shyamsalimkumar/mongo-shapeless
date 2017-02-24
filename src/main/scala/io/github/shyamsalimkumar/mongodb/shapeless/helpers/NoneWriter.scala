package io.github.shyamsalimkumar.mongodb.shapeless.helpers

sealed trait NoneWriter[A] { def shouldWrite: Boolean }

sealed trait NoneHandler { def writeNull: Boolean }

case class IgnoreNoneHandler() extends NoneHandler {
  override def writeNull: Boolean = false
}

case class ExplicitNullNoneHandler() extends NoneHandler {
  override def writeNull: Boolean = true
}

trait LowPriorityNoneWriterInstances {
  implicit val explicitNullNoneWriter = new NoneWriter[ExplicitNullNoneHandler] {
    def shouldWrite() = ExplicitNullNoneHandler().writeNull
  }
}

object NoneHandler extends LowPriorityNoneWriterInstances {
  implicit val ignoreNoneWriter = new NoneWriter[NoneHandler] {
    def shouldWrite() = IgnoreNoneHandler().writeNull
  }
}

object HandleNone {
  def shouldWrite[T <: NoneHandler: NoneWriter](implicit noneWriter: NoneWriter[T]): Boolean = noneWriter.shouldWrite
}