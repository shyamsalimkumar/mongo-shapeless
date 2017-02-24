name := "mongodb-shapeless"

version := "0.0.1"

scalaVersion := "2.11.8"

organization := "io.github.shyamsalimkumar"

resolvers ++= Seq()

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishMavenStyle := true

val mongoDbScalaDriverVersion = "1.2.1"

val shapelessVersion = "2.3.2"

val scalaTestVersion = "2.2.6"

val mongoDbScalaDriver = "org.mongodb.scala" %% "mongo-scala-driver" % mongoDbScalaDriverVersion

val shapeless = "com.chuusai" %% "shapeless" % shapelessVersion

val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % Test

val scalactic = "org.scalactic" %% "scalactic" % scalaTestVersion % Test

libraryDependencies ++= Seq(shapeless,
                            mongoDbScalaDriver,
                            scalactic,
                            scalaTest)

import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

SbtScalariform.scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignArguments, true)
  .setPreference(AlignParameters, true)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(MultilineScaladocCommentsStartOnFirstLine, true)
  .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, false)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(IndentLocalDefs, true)
  .setPreference(IndentSpaces, 2)
  .setPreference(PreserveSpaceBeforeArguments, true)
  .setPreference(RewriteArrowSymbols, true)
  .setPreference(DanglingCloseParenthesis, Force)
