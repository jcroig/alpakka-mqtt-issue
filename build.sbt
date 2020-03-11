name := "alpakka-mqtt-streaming-issue"

version := "1.0"

scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka"   %% "akka-stream-typed" % akkaVersion,
  "com.lightbend.akka" %% "akka-stream-alpakka-mqtt"           % "2.0.0-M3",
  "com.lightbend.akka" %% "akka-stream-alpakka-mqtt-streaming" % "2.0.0-M3"
)
