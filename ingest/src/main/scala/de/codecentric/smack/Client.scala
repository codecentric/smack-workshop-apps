package de.codecentric.smack

import akka.actor.ActorSystem
import akka.{Done, NotUsed}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.lambdaworks.jacks.JacksMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}


import scala.concurrent.Promise

object Client {
  def main(args: Array[String]) = {


    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    import system.dispatcher


    /**
      * Settings for a Kafka Sink
      * Contains Serializers for Kafka keys and values
      */
    val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers(Config.kafkaConnectionString)
    Producer.plainSink(producerSettings)


    /**
      * Implement this method by delegating to JsonHandler.mapJson
      *
      * @param msg A websocket Message
      * @return A Track Option that either contains a track if the message was parseable or None if not
      */
    def mapIncomingWSMessage(msg: Message): Option[Track] = JsonHandler.mapJson(msg)


    /**
      * Implement this method
      * You can assume that the Option contains a valid Track, you can access it with opt.get
      * You are free to choose a constructor for ProducerRecord, but we recommend
      * https://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/producer/ProducerRecord.html#ProducerRecord(java.lang.String,%20V)
      *
      * You have to pass the topic, use Config.kafkaTopicq
      *
      * @param opt
      * @return
      */
    def mapOptionalTrackToProducerRecord(opt: Option[Track]): ProducerRecord[Array[Byte], String] = ???



    // A Sink that reads Message objects and transforms them into ProducerRecord objects to send to Kafka
    val kafkaSink: Sink[Message, NotUsed] = Flow[Message].map(mapIncomingWSMessage(_))
      .filter(_.isDefined)
      .map(mapOptionalTrackToProducerRecord(_))
      .to(Producer.plainSink(producerSettings))


    //Creates a Flow object that connects a source that emits Message objects to a Sink that accepts Message objects
    val flow: Flow[Message, Message, Promise[Option[Message]]] =
      Flow.fromSinkAndSourceMat(kafkaSink, Source.maybe[Message])(Keep.right)


    //opens Websocket connection and connects the flow to it
    val (upgradeResponse, closed) =
      Http().singleWebSocketRequest(WebSocketRequest(Config.websocketUrl), flow)


    //ensures that the WS is running
    val upgrade = upgradeResponse.map { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        Done
      } else {
        throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
      }
    }





  }
}
