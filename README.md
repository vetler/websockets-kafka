[![Build Status](https://cloud.drone.io/api/badges/vetler/websockets-kafka/status.svg)](https://cloud.drone.io/vetler/websockets-kafka)
websockets-kafka
====================

An experiment to let web pages access Apache Kafka through Javascript and WebSockets.

## Apache Kafka

Start Apache Kafka, as described in the [quickstart](http://kafka.apache.org/documentation.html#quickstart). The Zookeeper configuration in the server is not yet configurable, so it is important that it is running on the default port.

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

Point your browser to [localhost:8080](http://localhost:8080). There is not really a UI yet, but check the Javascript console, and messages will appear as they are received.

## Sending messages

To send messages, just use `kafka-console-producer.sh`:

```
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic testing-topic
```

Each line you type will be echoed in the browser.
