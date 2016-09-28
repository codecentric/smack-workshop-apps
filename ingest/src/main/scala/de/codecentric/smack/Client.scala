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




    val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers("localhost:9092")
    Producer.plainSink(producerSettings)



    val kafkaSink: Sink[Message, NotUsed] = Flow[Message].map(mapJson)
      .filter(optionalTrack => optionalTrack.isDefined)
      .map(optionalTrack => JacksMapper.writeValueAsString(optionalTrack.get))
      .map(new ProducerRecord[Array[Byte], String]("topic", _))
      .to(Producer.plainSink(producerSettings))


    val flow: Flow[Message, Message, Promise[Option[Message]]] =
      Flow.fromSinkAndSourceMat(kafkaSink, Source.maybe[Message])(Keep.right)


    val (upgradeResponse, closed) =
      Http().singleWebSocketRequest(WebSocketRequest("ws://localhost:8080/websocket-echo"), flow)

    val upgrade = upgradeResponse.map { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        Done
      } else {
        throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
      }
    }



    /**
      * Implement this method by delegating to JsonHandler.mapJson
      *
      * @param msg A websocket Message
      * @return A Track Option that either contains a track if the message was parseable or None if not
      */
    def mapJson(msg: Message): Option[Track] = ???


  }
}
