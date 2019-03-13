package streaming

import java.util.Properties

import scala.reflect.runtime.currentMirror

object EnvironmentProperties {
  /**
    * Returns a properties object containing necessary runtime parameters,
    * taken from Spark's environment variables.
    */
  def getRuntimeProperties: Properties = {
    val mirror = currentMirror reflect EnvironmentVariables
    val mirrorVars = mirror.symbol.asClass.typeSignature.members filter (s => s.isTerm && s.asTerm.isAccessor)
    val envVariables: Iterable[Any] = mirrorVars map (mirror reflectMethod _.asMethod) map (_.apply())

    val props = new Properties()
    envVariables.foreach(env => envToProp(props, env.toString))

    props
  }

  def envToProp(props: Properties, key: String): Unit = {
    if (sys.env.contains(key)) props.setProperty(key, sys.env(key))
  }
}
