package de.codecentric.smack

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import com.fasterxml.jackson.databind.node.ObjectNode
import com.lambdaworks.jacks.JacksMapper

/**
  * Created by ftr on 28/09/16.
  */
object JsonHandler {
  def mapJson(msg: Message): Option[Track] = {
    try {
      msg match {
        case message: TextMessage.Strict => {
          val msg = message.text
          val on = JacksMapper.readValue[ObjectNode](msg)
          println(msg)

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
}
