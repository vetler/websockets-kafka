import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.{ExecutorService, Executors}

import MsgPack.valueToString
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
class KafkaWebSocketServer(port: InetSocketAddress) extends WebSocketServer(port) with LazyLogging {

  import KafkaWebSocketServer.pool

  var consumers = Map[WebSocket, ConsumeBridge]()

  override def onOpen(conn: WebSocket, handshake: ClientHandshake) {
    logger.debug(s"New connection established: ${conn.getRemoteSocketAddress}")
  }

  def initialize(socket: WebSocket, message: MsgPack): Unit = {
    val webSocketConsumer = new ConsumeBridge(message.get("groupId"), message.get("topic"), socket)
    pool.execute(webSocketConsumer)
    consumers = consumers + (socket -> webSocketConsumer)
  }

  override def onMessage(conn: WebSocket, bytes: ByteBuffer): Unit = {
    val message = new MsgPack(bytes)
    logger.debug(s"Message received: ${message}")

    message.get("command") match {
      case v: RawValue if v.getString.equals("init") => initialize(conn, message)
    }
  }

  override def onMessage(conn: WebSocket, message: String): Unit = {
    logger.debug(s"String message received: $message")
  }

  override def onError(conn: WebSocket, ex: Exception) {
    logger.debug(s"Error: ${ex.getMessage}")
  }

  override def onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean): Unit = {
    logger.debug(s"Connection closed: ${conn.getRemoteSocketAddress}")
    consumers.get(conn).foreach(_.stopProcessing)
    consumers = consumers - conn
  }
}

object KafkaWebSocketServer {
  val pool: ExecutorService = Executors.newCachedThreadPool()

  def main(args: Array[String]) {
    WebSocketImpl.DEBUG = true;
    new KafkaWebSocketServer(new InetSocketAddress(9003)).start();
  }
}
