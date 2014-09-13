import java.util.Properties
import java.util.concurrent.atomic.AtomicReference

import com.typesafe.scalalogging.slf4j.LazyLogging
import kafka.consumer.{ConsumerConfig, KafkaStream}
import kafka.javaapi.consumer.ConsumerConnector
import kafka.message.MessageAndMetadata
import org.java_websocket.WebSocket

import scala.collection.JavaConverters._

/**
 * Starts a Kafka consumer connector and listens to messages on a specific topic, with a given group ID. Every time a message is received,
 * it is sent to the websocket.
 *
 * @param groupId The consumer group id
 * @param topic The topic that should be listened to
 * @param connection The websocket connection
 */
class ConsumeBridge(groupId: String, topic: String, connection: WebSocket) extends Runnable with LazyLogging {

  lazy val consumerConfig = {
    val builder = new ConsumerConfigBuilder
    builder +=("group.id", groupId)
    builder +=("zookeeper.connect", "localhost:2181")
    builder +=("zookeeper.session.timeout.ms", "400")
    builder +=("zookeeper.sync.time.ms", "200")
    builder +=("auto.commit.interval.ms", "1000")
    builder.build
  }
  
  lazy val connector = {
    kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig)
  }
  
  var keepProcessing = new AtomicReference(true)

  /**
   * We are unable to interrupt the thread immediately, but this will make it stop when it receives a message.
   * @todo Improve this somehow?
   */
  def stopProcessing {
    keepProcessing.set(false)
  }

  override def run(): Unit = {
    processStream(createStream(connector))
    connector.shutdown()
  }

  def createStream(consumerConnector: ConsumerConnector) = {
    consumerConnector.createMessageStreams(Map(topic -> Integer.valueOf(1)).asJava).get(topic).get(0)
  }

  def processStream(kafkaStream: KafkaStream[Array[Byte], Array[Byte]]) {
    kafkaStream.iterator().toStream.takeWhile(_ => keepProcessing.get).foreach(processEvent)
  }

  def processEvent(event: MessageAndMetadata[Array[Byte], Array[Byte]]) {
    logger.debug(s"Received message: ${new String(event.message())}")
    connection.send(new String(event.message()))
  }

}

class ConsumerConfigBuilder {
  val properties = new Properties

  def +=(elem: (String, String)): ConsumerConfigBuilder = {
    (properties.put _).tupled(elem)
    this
  }

  def build(): ConsumerConfig = new ConsumerConfig(properties)
}