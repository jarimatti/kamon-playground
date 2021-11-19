addSbtPlugin("io.kamon" % "sbt-kanela-runner" % "2.0.12")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.7")
// sbt-javaagent allows us to add Kanela to the start scripts.
addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.6")