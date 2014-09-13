name := "websockets-kafka-server"

mainClass in Compile := Some("WebSocketKafkaServer")

libraryDependencies += "org.java-websocket" % "Java-WebSocket" % "1.3.0"

libraryDependencies += "org.msgpack" %% "msgpack-scala" % "0.6.11"

//libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

// Logging
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"

libraryDependencies ++= Seq(
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.0.2",
  "org.apache.logging.log4j" % "log4j-api" % "2.0.2",
  "org.apache.logging.log4j" % "log4j-core" % "2.0.2",
  "org.apache.logging.log4j" % "log4j-1.2-api" % "2.0.2"
)

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka_2.10" % "0.8.1.1"
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri")
    exclude("log4j", "log4j")
)

// Testing
libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.10" % "2.2.1" % "test",
  "org.easymock" % "easymock" % "3.2" % "test"
)

