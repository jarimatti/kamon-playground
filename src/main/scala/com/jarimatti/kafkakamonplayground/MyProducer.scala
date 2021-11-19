package com.jarimatti.kafkakamonplayground

import kamon.Kamon
import kamon.trace.Span
import kamon.trace.Trace.SamplingDecision
import org.apache.kafka.clients.producer.{KafkaProducer, Producer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.Serdes
import org.slf4j.LoggerFactory

import java.time.Instant
import java.util.Properties
import scala.concurrent.ExecutionContext

object MyProducer extends App {
  Kamon.init()

  val log = LoggerFactory.getLogger(MyProducer.getClass)
  log.info("Producer starting")

  val topic = "my-topic"

  val props: Properties = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(ProducerConfig.ACKS_CONFIG, "all")
  props.put(ProducerConfig.RETRIES_CONFIG, "0")
  props.put(ProducerConfig.LINGER_MS_CONFIG, "1")

  log.info("Building span")
  val span = Kamon.producerSpanBuilder("myOperation", "MyProducer")
    .tag("hello", "world")
    .samplingDecision(SamplingDecision.Sample)
    .trackMetrics()
    .start()
  log.info("Built span")

  Kamon.runWithSpan(span) {
    log.info(s"Current span: ${Kamon.currentSpan()}")
    log.info(s"Current span ID: ${Kamon.currentSpan().id.string}")
    log.info(s"Current context: ${Kamon.currentContext()}")
    log.info(s"Current span trace ID: ${Kamon.currentSpan().trace.id.string}")

    Kamon.currentSpan().mark("producer.start")

    val keySerdes = Serdes.String()
    val valueSerdes = Serdes.String()
    val producer: Producer[String, String] = new KafkaProducer[String, String](
      props, keySerdes.serializer(), valueSerdes.serializer())

    val producerRecord = new ProducerRecord[String, String](
      topic,
      "key",
      s"Hello, World! ${Instant.now()}"
    )

    val metadata = producer.send(producerRecord).get()

    Kamon.currentSpan().mark("producer.sent")
    log.info(s"Sent: ${metadata.toString}")

    producer.close()
    Kamon.currentSpan().mark("producer.closed")
  }

  Thread.sleep(500)
  span.finish()
  Thread.sleep(500)

  implicit val ec: ExecutionContext = ExecutionContext.global
  Kamon.stop().onComplete(_ => {
    log.info("Kamon stopped")
  })
}
