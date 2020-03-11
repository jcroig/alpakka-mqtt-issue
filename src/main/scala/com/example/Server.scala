package com.example

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.OverflowStrategy
import akka.stream.alpakka.mqtt.streaming.ControlPacketFlags._
import akka.stream.alpakka.mqtt.streaming._
import akka.stream.alpakka.mqtt.streaming.scaladsl.{ActorMqttServerSession, Mqtt}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source, Tcp}
import akka.util.ByteString

object Server extends App {

  implicit val as = ActorSystem()

  val settings = MqttSessionSettings()
  val session = ActorMqttServerSession(settings)

  Tcp()
    .bind("0.0.0.0", 1883)
    .flatMapMerge(
      10, { connection =>
        val mqttFlow: Flow[Command[Nothing], Either[MqttCodec.DecodeError, Event[Nothing]], NotUsed] =
          Mqtt
            .serverSessionFlow(session, ByteString(connection.remoteAddress.getAddress.getAddress))
            .join(connection.flow)

        val (queue, source) = Source
          .queue[Command[Nothing]](10, OverflowStrategy.dropHead)
          .via(mqttFlow)
          .toMat(BroadcastHub.sink)(Keep.both)
          .run()

        source
          .runForeach {
            case Right(Event(PingReq, _)) =>
              queue.offer(Command(PingResp))

            case Right(Event(_: Connect, _)) =>
              queue.offer(Command(ConnAck(ConnAckFlags.None, ConnAckReturnCode.ConnectionAccepted)))

            case Right(Event(Publish(flags, _, Some(packetId), _), _)) if flags.contains(QoSExactlyOnceDelivery) =>
              queue.offer(Command(PubRec(packetId)))

            case Right(Event(PubRel(packetId), _)) =>
              queue.offer(Command(PubComp(packetId)))

            case _ =>
          }

        source
      }
    )
    .to(Sink.ignore)
    .run()
}
