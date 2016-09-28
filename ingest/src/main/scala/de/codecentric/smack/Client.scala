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


    def processMessage(msg: Message): Option[Track] = {
      try {
        msg match {
          case message: TextMessage.Strict => {
            val msg = message.text
            val on = JacksMapper.readValue[ObjectNode](msg)

            val evt = on.get("eventElement").get(0)

            import collection.JavaConverters._

            val albumName = evt.get("album").get("name").asText()
            val artists = evt.get("artists").iterator().asScala.map(artist => Artist(artist.get("name").asText())).toList
            val discNumber = evt.get("disc_number").asDouble()
            val duration = evt.get("duration_ms").asLong()
            val explicit = evt.get("explicit").asBoolean()
            val id = evt.get("id").asText()
            val isPlayable = if (evt.get("is_playable") != null) evt.get("is_playable").asBoolean() else true
            val name = evt.get("name").asText()
            val popularity = evt.get("popularity").asDouble()
            val trackNumber = evt.get("track_number").asDouble()


            val track = Track(albumName, artists, discNumber, duration, explicit, id, isPlayable, name, popularity, trackNumber)


            Some(track)

          }

          case _ => None
        }
      } catch {
        case ex: Exception => {
          ex.printStackTrace()
          None
        }
      }
    }

    val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers("localhost:9092")
    Producer.plainSink(producerSettings)



    val kafkaSink: Sink[Message, NotUsed] = Flow[Message].map(processMessage(_))
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


  }
}
