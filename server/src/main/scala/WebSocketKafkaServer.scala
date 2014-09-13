import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.{ExecutorService, Executors}

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.java_websocket.{WebSocket, WebSocketImpl}
import org.msgpack.`type`.RawValue


/**
 * Websocket server for redirecting Apache Kafka events to websocket clients.
 *
 * @param port The port the server should listen to
 */
class WebSocketKafkaServer(port: InetSocketAddress) extends WebSocketServer(port) with LazyLogging {

  import WebSocketKafkaServer.pool

  var consumers = Map[WebSocket, ConsumerWebSocketBridge]()


  override def onOpen(conn: WebSocket, handshake: ClientHandshake) {
    logger.debug(s"New connection established: ${conn.getRemoteSocketAddress}")
  }

  override def onError(conn: WebSocket, ex: Exception) {
    logger.debug(s"Error: ${ex.getMessage}")
  }

  implicit def valueToString(v: RawValue) = v.getString

  def initialize(socket: WebSocket, message: MessagePackMap): Unit = {
    val groupId: String = message.get("groupId")
    val topic: String = message.get("topic")
    val webSocketConsumer = new ConsumerWebSocketBridge(groupId, topic, socket)
    pool.execute(webSocketConsumer)
    consumers = consumers + (socket -> webSocketConsumer)
  }

  override def onMessage(conn: WebSocket, bytes: ByteBuffer): Unit = {
    val message = new MessagePackMap(bytes)
    logger.debug(s"Message received: ${message}")

    message.get("command") match {
      case v: RawValue if v.getString.equals("init") => initialize(conn, message)
    }
  }

  override def onMessage(conn: WebSocket, message: String): Unit = {
    logger.debug(s"String message received: $message")
  }

  override def onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean): Unit = {
    logger.debug(s"Connection closed: ${conn.getRemoteSocketAddress}")
    consumers.get(conn) match {
      case Some(consumer: ConsumerWebSocketBridge) => consumer.interrupt
      case None => throw new IllegalStateException(s"No consumer bridge found for connection $conn")
    }
    consumers = consumers - conn
  }
}

object WebSocketKafkaServer {
  val pool: ExecutorService = Executors.newCachedThreadPool()

  def main(args: Array[String]) {
    WebSocketImpl.DEBUG = true;
    new WebSocketKafkaServer(new InetSocketAddress(9003)).start();
  }
}
