name := "Twitter-Streaming"

version := "0.1"

scalaVersion := "2.11.2"

// Main class definition.
// Print task is used by deploy script.
val aukletMainClass = "twitter.streaming.MainStream"
mainClass in (Compile,run) := Some(aukletMainClass)
val printMainClass = TaskKey[Unit]("printMainClass", "Print Auklet Spark app's main class")
printMainClass := println(aukletMainClass)

//
// *** Dependency configuration ***
//
resolvers ++= Seq(
  "bintray-spark-packages" at "https://dl.bintray.com/spark-packages/maven/"
)

val sparkVersion = "2.2.1"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion
)

//
// *** Run configuration ***
//

// Include provided scope dependencies when running locally (e.g. sbt run)
run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run)).evaluated