websockets-kafka
====================

An experiment to let web pages access Apache Kafka through Javascript and WebSockets.

## Server

To start the server, run the `main` method in the `KafkaWebSocketServer` object. You can do this by
running SBT like this:

```
sbt "project server" run
```
 

## Client

To start the client, run the `main` method in the `ConsumerClientServer`. You can
do this by running SBT like this:

```
sbt "project client" run
```

## Sending messages

To send messages, just use `kafka-console-producer.sh`:

```
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic testing-topic
```

Each line you type will be echoed in the browser.