package com.jarimatti.kafkakamonplayground

import com.typesafe.config.ConfigFactory
import kamon.Kamon
import kamon.instrumentation.kafka.client.KafkaInstrumentation
import kamon.trace.Span
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.serialization.Serdes
import org.slf4j.LoggerFactory

import java.time.Duration
import java.util.Properties
import scala.jdk.CollectionConverters._

object MyConsumer extends App {
  val config = ConfigFactory.parseString("kamon.environment.service = MyConsumer").withFallback(ConfigFactory.load())
  Kamon.init(config)
  val log = LoggerFactory.getLogger(MyProducer.getClass)

  log.info("Consumer starting")

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
  log.info(s"Current context: ${Kamon.currentContext()}")
  log.info(s"Current span trace ID: ${Kamon.currentSpan().trace.id.string}")
  log.info(s"Current span ID: ${Kamon.currentSpan().id.string}")
  log.info(s"Current span parent ID: ${Kamon.currentSpan().parentId.string}")

  try {
    while (true) {
      val records = consumer.poll(Duration.ofSeconds(1))
      records.iterator().forEachRemaining(record => KafkaInstrumentation.runWithConsumerSpan(record) {
        Kamon.currentSpan().mark("consumer.start")
        log.info(s"Current context: ${Kamon.currentContext()}")
        log.info(s"Current context tags: ${Kamon.currentContext().tags}")
        log.info(s"Is span empty: ${Kamon.currentSpan().isEmpty}")
        log.info(s"Is span remote: ${Kamon.currentSpan().isRemote}")
        log.info(s"Current span trace ID: ${Kamon.currentSpan().trace.id.string}")
        log.info(s"Current span ID: ${Kamon.currentSpan().id.string}")
        log.info(s"Current span parent ID: ${Kamon.currentSpan().parentId.string}")

        // Interesting: calling this manually at this point returns an empty context.
        val incomingContext = KafkaInstrumentation.extractContext(record)
        log.info(s"Incoming context: ${incomingContext}")
        log.info(s"Incoming context span ID: ${incomingContext.get(Span.Key).id.string}")
        log.info(s"Incoming context span parent ID: ${incomingContext.get(Span.Key).parentId.string}")
        log.info(s"Incoming context nonEmpty(): ${incomingContext.nonEmpty()}")

        log.info(s"Incoming record: ${record}")
        Thread.sleep(1000)
        Kamon.currentSpan().mark("consumer.done")
      })
    }
  } finally {
    consumer.close()
  }
}
