package streaming

import java.util.Properties

object PostgresSetup {
  private val dropLogTable = "DROP TABLE IF EXISTS log"
  private var properties: Properties = _

  /**
    * Create the log table for the DB
    */
  def setUp(): Unit = {
    if (properties == null) {
      properties = EnvironmentProperties.getRuntimeProperties
      PostgresClient.waitUntilAvailable(properties)
      PostgresClient.props = properties
      createLogTable()
    }
  }

  private def createLogTable(): Unit = {
    val postgres = PostgresClient.startPostgres()
    val stmt = postgres.createStatement()
    stmt.executeUpdate(dropLogTable)

    stmt.executeUpdate(
      """
        |CREATE TABLE log (
        |    log_id serial PRIMARY KEY,
        |    log_date date,
        |    computer character varying(256) NOT NULL,
        |    log_time time without time zone,
        |    session_id integer NOT NULL,
        |    user_name character varying(256) NOT NULL,
        |    event_type character varying(1024) NOT NULL,
        |    process_id integer,
        |    process_name character varying(256)
        |);
      """.stripMargin)
    stmt.close()

    stmt.executeUpdate(
      """
        |CREATE TABLE session (
        |    serial_id serial PRIMARY KEY,
        |    session_date date,
        |    session_id,
        |    computer_name
        |    session_time
        |    session_user
        |    session_length
        |    session_event
        |);
      """.stripMargin)
    stmt.close()

    PostgresClient.stopPostgres(postgres)
  }
}
