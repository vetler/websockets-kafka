import java.util.Properties
import java.util.concurrent.atomic.AtomicReference

import com.typesafe.scalalogging.slf4j.LazyLogging
import kafka.consumer.ConsumerConfig
import kafka.message.MessageAndMetadata
import org.java_websocket.WebSocket

import scala.collection.JavaConverters._

class ConsumerWebSocketBridge(groupId: String, topic: String, connection: WebSocket) extends Runnable with LazyLogging {

  lazy val consumerConfig = {
    val builder = new ConsumerConfigBuilder
    builder +=("group.id", groupId)
    builder +=("zookeeper.connect", "localhost:2181")
    builder +=("zookeeper.session.timeout.ms", "400")
    builder +=("zookeeper.sync.time.ms", "200")
    builder +=("auto.commit.interval.ms", "1000")
    builder.result
  }

  var keepProcessing = new AtomicReference(true)

  def interrupt {
    keepProcessing.set(false)
  }

  override def run(): Unit = {
    val consumerConnector = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig)
    val topicThreads = Map(topic -> Integer.valueOf(1))
    val stream = consumerConnector.createMessageStreams(topicThreads.asJava).get(topic).get(0)

    stream.iterator().toStream.takeWhile(foo).foreach(processEvent)
    consumerConnector.shutdown()
  }

  def foo(event: MessageAndMetadata[Array[Byte], Array[Byte]]) = {
    keepProcessing.get()
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