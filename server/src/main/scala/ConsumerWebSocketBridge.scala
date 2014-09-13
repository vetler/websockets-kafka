import java.util.Properties

import com.typesafe.scalalogging.slf4j.LazyLogging
import kafka.consumer.ConsumerConfig
import kafka.message.MessageAndMetadata
import org.java_websocket.WebSocket

import scala.collection.JavaConverters._

class ConsumerWebSocketBridge(id: String, connection: WebSocket) extends Runnable with LazyLogging {

  val consumerConfig = {
    val builder = new ConsumerConfigBuilder
    builder += ("group.id", id)
    builder += ("zookeeper.connect", "localhost:2181")
    builder += ("zookeeper.session.timeout.ms", "400")
    builder += ("zookeeper.sync.time.ms", "200")
    builder += ("auto.commit.interval.ms", "1000")
    builder.result
  }

  override def run(): Unit = {
    val consumerConnector = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig)
    val stream = consumerConnector.createMessageStreams(Map(id -> Integer.valueOf(1)).asJava).get(id).get(0)

    stream.iterator().foreach(processEvent)
  }

  def processEvent(event: MessageAndMetadata[Array[Byte], Array[Byte]]) {
    logger.debug(s"Received message: ${new String(event.message())}")
    connection.send(new String(event.message()))
  }
}

class ConsumerConfigBuilder /* extends mutable.Builder[(String, String), ConsumerConfig]*/ {
  val properties = new Properties

  /*override */ def +=(elem: (String, String)): ConsumerConfigBuilder = {
    (properties.put _).tupled(elem)
    this
  }

  /*override */ def result(): ConsumerConfig = new ConsumerConfig(properties)

  /*override */ def clear() = properties.clear()
}