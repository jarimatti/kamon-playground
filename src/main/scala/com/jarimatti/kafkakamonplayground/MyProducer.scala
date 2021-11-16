package com.jarimatti.kafkakamonplayground

import org.apache.kafka.clients.producer.{KafkaProducer, Producer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.Serdes

import java.util.Properties

object MyProducer extends App {
  println("Producer starting")

  val topic = "my-topic"

  val props: Properties = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ProducerConfig.ACKS_CONFIG, "all")
  props.put(ProducerConfig.RETRIES_CONFIG, 0)
  props.put(ProducerConfig.LINGER_MS_CONFIG, 1)

  val keySerdes = Serdes.String()
  val valueSerdes = Serdes.String()
  val producer: Producer[String, String] = new KafkaProducer[String, String](
    props, keySerdes.serializer(), valueSerdes.serializer())

  val producerRecord = new ProducerRecord[String, String](
    topic,
    "key",
    "Hello, World!"
  )

  val metadata = producer.send(producerRecord).get()

  println(s"Sent: ${metadata.toString}")

  producer.close()
}
