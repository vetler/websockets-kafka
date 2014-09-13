import net.roeim.minihttpserver.MiniHttpServer

object WebSocketKafkaClientServer extends MiniHttpServer {
  get("/") { exchange =>
    exchange.getResponseHeaders.add("Content-type", "text/html")

    val page = <html>
      <script>
        <![CDATA[
        var connection = new WebSocket('ws://localhost:9003/');
        connection.onopen = function(){
            console.log('Connection open!');
            connection.send('Hey server, whats up?');
        }

        connection.onclose = function(){
            console.log('Connection closed');
        }

        connection.onmessage = function(e){
            var server_message = e.data;
            console.log(server_message);
        }

        connection.onerror = function(error){
            console.log('Error detected: ' + error);
        }

    ]]>
      </script>
      <body></body>
    </html>

    page.toString
  }

  def main(args: Array[String]) {
    WebSocketKafkaClientServer.start()
  }
}