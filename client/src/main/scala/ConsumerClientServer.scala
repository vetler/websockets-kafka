import net.roeim.minihttpserver.MiniHttpServer

object ConsumerClientServer extends MiniHttpServer {
  get("/") { exchange =>
    exchange.getResponseHeaders.add("Content-type", "text/html")

    val page = <html>
      <head>
        <script src="http://fb.me/react-0.11.1.js"></script>
        <script src="scripts/msgpack.js"></script>
        <script src="scripts/main.js"></script>
      </head>
      <body><ol id="messages"></ol></body>
    </html>

    page.toString
  }

  get("/scripts/msgpack.js") {
    exchange =>
      readResource("scripts/msgpack.js")
  }

  get("/scripts/main.js") {
    exchange =>
      readResource("scripts/main.js")
  }

  def readResource(resource: String) = {
    scala.io.Source.fromInputStream(getClass.getResourceAsStream(resource)).mkString
  }

  def main(args: Array[String]) {
    ConsumerClientServer.start()
  }
}