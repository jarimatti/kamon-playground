package com.jarimatti.kafkakamonplayground

import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.serialization.Serdes

import java.time.Duration
import java.util.Properties
import scala.jdk.CollectionConverters._

object MyConsumer extends App {
  println("Consumer starting")

  val topic = "my-topic"

  val props: Properties = new Properties()
  props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ConsumerConfig.GROUP_ID_CONFIG, "test")
  props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
  props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")

  val keySerdes = Serdes.String()
  val valueSerdes = Serdes.String()
  val consumer = new KafkaConsumer[String, String](
    props, keySerdes.deserializer(), valueSerdes.deserializer())

  consumer.subscribe(Seq(topic).asJava)

  try {
    while (true) {
      val records = consumer.poll(Duration.ofSeconds(1))
      val iterator = records.iterator()
      while (iterator.hasNext) {
        val record = iterator.next()
        println(record.toString)
      }
    }
  } finally {
    consumer.close()
  }
}
