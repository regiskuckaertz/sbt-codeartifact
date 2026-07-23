sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("io.github.regiskuckaertz.sbt" % "sbt-codeartifact" % x)
  case _        => sys.error("The system property 'plugin.version' is not defined.")
}
