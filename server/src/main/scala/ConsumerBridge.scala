import java.util
import java.util.Properties
import java.util.concurrent.atomic.AtomicReference

import com.typesafe.scalalogging.slf4j.LazyLogging
import kafka.consumer.{ConsumerConfig, KafkaStream}
import kafka.javaapi.consumer.ConsumerConnector
import kafka.message.MessageAndMetadata
import org.java_websocket.WebSocket

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

/**
 * Starts a Kafka consumer connector and listens to messages on a list of topics, with a given group ID. Every time a message is received,
 * it is sent to the websocket.
 *
 * @param groupId The consumer group id
 * @param topicList The topics that should be listened to
 * @param connection The websocket connection
 */
class ConsumerBridge(groupId: String, topicList: List[String], connection: WebSocket) extends LazyLogging {

  val topics = topicList

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

  def continueProcessing = keepProcessing.get

  def stopProcessing {
    keepProcessing.set(false)
  }

  def run() {
    processStreams(createStreams(connector))
    connector.shutdown()
  }

  def createStreams(consumerConnector: ConsumerConnector) = {
    val topicThreads = topics.map(topic => topic -> Integer.valueOf(1)).toMap
    consumerConnector.createMessageStreams(topicThreads.asJava).asScala
  }

  /**
   * Process incoming messages from streams. Reading from Kafka is blocking, but this will pause regularly
   * to see if we should stop.
   *
   * @param topicStreams
   */
  def processStreams(topicStreams: mutable.Map[String, util.List[KafkaStream[Array[Byte], Array[Byte]]]]) = {
    val processing = Future.sequence(topicStreams.map(entry => processStream(streamOf(entry))))
    while (continueProcessing) {
      try {
        Await.result(processing, 1 second)
      }
      catch {
        case e: TimeoutException =>
      }
    }
    logger.debug("Finished processing streams in ConsumerBridge")
  }
  
  def streamOf(entry: (String, util.List[KafkaStream[Array[Byte], Array[Byte]]])) = {
    entry._2.get(0)
  }

  def processStream(kafkaStream: KafkaStream[Array[Byte], Array[Byte]]) = {
    Future {
      blocking {
        kafkaStream.iterator().foreach(readMessage)
      }
    }
  }

  def readMessage(data: MessageAndMetadata[Array[Byte], Array[Byte]]) {
    logger.debug(s"Received message: ${new String(data.message())}")
    connection.send(new String(data.message()))
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