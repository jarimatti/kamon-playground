name := "kamon-kafka"

version := "0.1"

scalaVersion := "2.13.7"

enablePlugins(JavaAppPackaging, JavaAgent)

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "2.8.1",

  "io.kamon" %% "kamon-bundle" % "2.3.1",
  "io.kamon" %% "kamon-jaeger" % "2.3.1",

  "ch.qos.logback" % "logback-classic" % "1.2.7",
  "org.slf4j" % "slf4j-api" % "1.7.32",
)

javaAgents += "io.kamon" % "kanela-agent" % "1.0.13"
