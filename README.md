# alpakka-mqtt-issue
Reproduces alpakka mqtt streaming QoS2 issue

1. Run Server.scala
2. Run Client.scala

You should get and exception because first publish message can not be completed in 2 seconds.

If you comment line Client.scala::23 or you modify the topic to some other topic different from the first publish message and run Client.scala again, no error is thrown.
