package com.example

import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttAsyncClient, MqttConnectOptions, MqttMessage}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.util.control.NonFatal

object Client extends App {
  val client   = new MqttAsyncClient("tcp://localhost:1883", "test", new MemoryPersistence())
  val connOpts = new MqttConnectOptions
  connOpts.setCleanSession(true)

  try {
    client.connect(connOpts).waitForCompletion(5000)

    val message1 = new MqttMessage("Hello world!".getBytes)
    message1.setQos(2)

    val message2 = new MqttMessage("Hello world again!".getBytes)
    message2.setQos(2)

    val delivery = client.publish("greetings", message1)
    client.publish("greetings", message2)

    for (_ <- 0 until 10) {
      if (isDeliveryCompleted(delivery)) {
        client.disconnect().waitForCompletion(5000)
        sys.exit(0)
      } else {
        Thread.sleep(200)
      }
    }
    sys.error("Message can not be published")
  } catch {
    case NonFatal(e) =>
      e.printStackTrace()
      client.disconnect().waitForCompletion(5000)
      sys.exit(1)
  }

  def isDeliveryCompleted(delivery: IMqttDeliveryToken): Boolean = delivery.getMessage == null
}
