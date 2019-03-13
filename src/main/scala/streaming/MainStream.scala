package streaming

import java.util.Properties

import streaming.EnvironmentVariables.{SparkAppName, SparkMaster}
import org.apache.log4j.Logger
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkException}

object MainStream {
  private val log: Logger = Logger.getLogger(getClass.getName)
  var ssc: StreamingContext = _

  /** Entry point. */
  def main(args: Array[String]) {
    // Set up Environmental Properties
    val props = EnvironmentProperties.getRuntimeProperties

    // Make sure databases are up.
    // databaseClient.waitUntilAvailable(props)

    // Construct the Spark streaming context.
    ssc = createStreamingContext(props)

    // Construct the Kafka stream and store it.
    val stream: InputDStream[String] = _  // constructStream(ssc)

    // parse and store stream
    StoreStream.storeStream(stream, props)

    startStreaming()
  }

  /**
    * Creates a streaming context, using the input properties object to define connections to the Spark masters and to Cassandra.
    */
  def createStreamingContext(props: Properties): StreamingContext = {
    val conf: SparkConf = new SparkConf()
    conf.setAppName(props.getProperty(SparkAppName))
    val sparkMaster = props.getProperty(SparkMaster, "")

    if (sparkMaster != "") {
      // Only set some Spark parameters when SPARK_MASTER is set.
      // This should not be set when running in a clustered environment (EMR).
      conf.setMaster(sparkMaster)
      conf.set("spark.executor.memory", "1g")
      conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    }

    new StreamingContext(conf, Seconds(2))
  }

  def startStreaming(): Unit = {
    // Start the tasks.
    log.info("Starting Spark Context.")
    var errorOccurred = true

    try {
      ssc.start()

      // Wait until an exception occurs or EMR shuts us down.
      ssc.awaitTermination()
    } catch {
      case e: InterruptedException => log.error("InterruptedException: Spark context was interrupted", e)
      case e: SparkException => checkSparkExceptions(e)
      case e: Throwable => log.error("Unknown non-SparkException occurred.", e)
    } finally {
      shutdownStream(errorOccurred)
    }
  }

  def checkSparkExceptions(e: SparkException): Unit = {
    // We don't log the throwable nor the stack trace because Spark logs them before ssc.awaitTermination is called
    e.getCause match {
      case cause: Throwable => log.error("Unknown SparkException occurred.", cause)
    }
  }

  def shutdownStream(errorOccurred: Boolean): Unit = {
    // Try all cleanup tasks to prevent any exceptions from unwinding the
    // call stack before the JVM has a chance to re-queue the EMR step.
    try {
      ssc.stop(stopSparkContext = true, stopGracefully = true)
      ssc.awaitTerminationOrTimeout(5000)
    } catch {
      // Any exception from the Spark context has already been logged.
      case _: Throwable =>
    }
  }

  def storeStream(stream: InputDStream[String], props: Properties): Unit = {
    stream.foreachRDD((rdd, time) => {
      // Create offsets range to save back to Kafka
      // val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

      // Parse, Authenticate, and store in a database
      rdd.foreachPartition(partition => {
        // We have to set the properties in the beginning of each rdd.foreachPartition() statement as spark moves all
        // the work to spark workers and setting the props object helps pass these objects to the workers and ensures
        // the drivers are not null
        // https://spark.apache.org/docs/latest/streaming-programming-guide.html#design-patterns-for-using-foreachrdd

        partition.foreach(record => storeData(record, props))
      })

      // Save offset range back into Kafka after the RDDs are done being processed
      // stream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
    })
  }

  def storeData(record: String, props: Properties): Unit = {
    // Do something with record
  }
}
