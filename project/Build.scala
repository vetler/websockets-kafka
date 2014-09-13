import sbt._

object WebSocketKafkaBuild extends Build {
  lazy val server = Project(id = "server",
    base = file("server"))
    .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)

  lazy val client = Project(id = "client",
    base = file("client"))
    .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
}