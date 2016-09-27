package de.codecentric.smack

import akka.actor.ActorSystem
import akka.{Done, NotUsed}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import com.fasterxml.jackson.databind.node.ObjectNode
import com.lambdaworks.jacks.JacksMapper

import scala.annotation.tailrec
import scala.concurrent.{Future, Promise}

object Client {
  def main(args: Array[String]) = {


    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    import system.dispatcher


    def processMessage(msg: Message) = {
      try{
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
            val isPlayable = if(evt.get("is_playable") != null)  evt.get("is_playable").asBoolean() else true
            val name = evt.get("name").asText()
            val popularity = evt.get("popularity").asDouble()
            val trackNumber = evt.get("track_number").asDouble()



         val track = Track(albumName, artists, discNumber, duration, explicit, id, isPlayable, name, popularity, trackNumber)


            println(track)

          }

          case _ => //do nothing for streamed TextMessages. This is just lazyness.
        }
      } catch {
        case ex: Exception => ex.printStackTrace()
      }
    }

    val flow: Flow[Message, Message, Promise[Option[Message]]] =
      Flow.fromSinkAndSourceMat(
        Sink.foreach[Message](processMessage(_)),
        Source.maybe[Message])(Keep.right)

    val (upgradeResponse, closed) =
      Http().singleWebSocketRequest(WebSocketRequest("ws://localhost:8080/websocket-echo"), flow)

    val connected = upgradeResponse.map { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        Done
      } else {
        throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
      }
    }


  }
}
