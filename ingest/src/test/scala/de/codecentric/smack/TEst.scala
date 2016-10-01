package de.codecentric.smack

import akka.http.scaladsl.model.ws.TextMessage
import org.scalatest._

/**
  * Created by ftr on 01/10/16.
  */
class TestSpec extends FlatSpec with Matchers {

  "The client" should "convert an original JSON message to a Track object" in {

    val textMessage = TextMessage("{\"eventElement\":[{\"disc_number\":1,\"album\":{\"images\":[{\"width\":640,\"url\":\"https://i.scdn.co/image/7884811229bac18514c166b15f1fe3e8ac4aec61\",\"height\":640},{\"width\":300,\"url\":\"https://i.scdn.co/image/b6c55789ad7aa4cabc919e95e4766a2dd621d53e\",\"height\":300},{\"width\":64,\"url\":\"https://i.scdn.co/image/23014400e42df5bf46b3d83f07d476233c2b169f\",\"height\":64}],\"name\":\"All I Wanna Do\",\"available_markets\":[\"AD\",\"AR\",\"AU\",\"BE\",\"BG\",\"BO\",\"BR\",\"CL\",\"CO\",\"CR\",\"CY\",\"CZ\",\"DK\",\"DO\",\"EC\",\"EE\",\"ES\",\"FI\",\"FR\",\"GR\",\"GT\",\"HK\",\"HN\",\"HU\",\"ID\",\"IS\",\"LT\",\"LU\",\"LV\",\"MC\",\"MX\",\"MY\",\"NI\",\"NL\",\"NO\",\"NZ\",\"PA\",\"PE\",\"PH\",\"PL\",\"PT\",\"PY\",\"SE\",\"SG\",\"SK\",\"SV\",\"TR\",\"TW\",\"US\",\"UY\"],\"album_type\":\"single\",\"href\":\"https://api.spotify.com/v1/albums/5iTIgNz59Z4j1wtX0I29z8\",\"id\":\"5iTIgNz59Z4j1wtX0I29z8\",\"type\":\"album\",\"external_urls\":{\"spotify\":\"https://open.spotify.com/album/5iTIgNz59Z4j1wtX0I29z8\"},\"uri\":\"spotify:album:5iTIgNz59Z4j1wtX0I29z8\"},\"available_markets\":[\"AD\",\"AR\",\"AU\",\"BE\",\"BG\",\"BO\",\"BR\",\"CL\",\"CO\",\"CR\",\"CY\",\"CZ\",\"DK\",\"DO\",\"EC\",\"EE\",\"ES\",\"FI\",\"FR\",\"GR\",\"GT\",\"HK\",\"HN\",\"HU\",\"ID\",\"IS\",\"LT\",\"LU\",\"LV\",\"MC\",\"MX\",\"MY\",\"NI\",\"NL\",\"NO\",\"NZ\",\"PA\",\"PE\",\"PH\",\"PL\",\"PT\",\"PY\",\"SE\",\"SG\",\"SK\",\"SV\",\"TR\",\"TW\",\"US\",\"UY\"],\"type\":\"track\",\"external_ids\":{\"isrc\":\"DK4YA1604801\"},\"uri\":\"spotify:track:0BiGd2FNDQ0eh79ZHfp6tS\",\"duration_ms\":194360,\"explicit\":false,\"artists\":[{\"name\":\"Martin Jensen\",\"href\":\"https://api.spotify.com/v1/artists/4ehtJnVumNf6xzSCDk8aLB\",\"id\":\"4ehtJnVumNf6xzSCDk8aLB\",\"type\":\"artist\",\"external_urls\":{\"spotify\":\"https://open.spotify.com/artist/4ehtJnVumNf6xzSCDk8aLB\"},\"uri\":\"spotify:artist:4ehtJnVumNf6xzSCDk8aLB\"}],\"preview_url\":\"https://p.scdn.co/mp3-preview/4bc990004716d76c99f5402d6aa5674584d13aca\",\"popularity\":76,\"name\":\"All I Wanna Do\",\"track_number\":1,\"href\":\"https://api.spotify.com/v1/tracks/0BiGd2FNDQ0eh79ZHfp6tS\",\"id\":\"0BiGd2FNDQ0eh79ZHfp6tS\",\"external_urls\":{\"spotify\":\"https://open.spotify.com/track/0BiGd2FNDQ0eh79ZHfp6tS\"}}],\"eventType\":[\"listenTrack\"]}")

    val trackOpt = Client.mapIncomingWSMessage(textMessage)

    assert(trackOpt.isDefined)

    val track = trackOpt.get

    assert(track.id === "")


  }

  "The client" should "convert an optional track to a producer record" in {

    val track = Track("The Rise and Fall of Ziggy Stardust and the Spiders from Mars", List(Artist("David Bowie")), 1, 3000, false, "asdf", true, "Ziggy Stardust", 100, 5)


    val rec = Client.mapOptionalTrackToProducerRecord(Option(track))

    assert(Option(rec) != None)
    assert(rec.topic() == Config.kafkaTopic)


  }



}
