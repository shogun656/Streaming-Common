package streaming

import java.sql.{Connection, SQLException}
import java.util.Properties

import tausight.EnvironmentVariables.{PostgresPassword, PostgresUrl, PostgresUsername}
import org.apache.commons.dbcp2.BasicDataSource

object PostgresClient {
  private var _props: Properties = _
  private var postgresPool: BasicDataSource = _

  /**
    * Grab a connection from Postgres pool
    */
  def startPostgres(): Connection = {
    postgresPool.getConnection()
  }

  def stopPostgres(postgres: Connection): Unit = {
    if (postgres != null) {
      postgres.close()
    }
  }

  /**
    * Create a pool postgres connections. Very useful for threaded applications
    */
  def createPostgresPool(): Unit = {
    postgresPool = new BasicDataSource
    postgresPool.setDriverClassName("org.postgresql.Driver")
    postgresPool.setUrl(props.getProperty(PostgresUrl))
    postgresPool.setUsername(props.getProperty(PostgresUsername))
    postgresPool.setPassword(props.getProperty(PostgresPassword))
    postgresPool.setInitialSize(2)
  }

  /**
    * Continue trying to connect to Postgres
    */
  def waitUntilAvailable(props: Properties): Unit = {
    _props = props
    var sleep: Int = 10000
    val sleepMax = 5 * (60 * 1000)
    var postgresIsDown = true

    Thread.sleep(sleep)
    while (postgresIsDown) {
      try {
        createPostgresPool()
        val postgres = startPostgres()
        stopPostgres(postgres)
        postgresIsDown = false
      } catch {
        case _: SQLException =>
          sleep *= 2
          if (sleep > sleepMax) {
            sleep = sleepMax
          }

          println(s"Postgres is still down, Spark is going to sleep another ${sleep / 1000} seconds")
          Thread.sleep(sleep)
      }
    }
  }

  def props: Properties = {
    if (_props == null) {
      throw new IllegalStateException("Properties shouldn't be null")
    }
    _props
  }

  def props_=(properties: Properties): Unit = {
    // Properties should only be set once
    if (_props == null) {
      _props = properties
      if (postgresPool == null) {
        createPostgresPool()
      }
    }
  }
}
