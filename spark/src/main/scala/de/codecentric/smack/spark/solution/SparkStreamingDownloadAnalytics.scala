/*
 * Copyright 2016 Matthias Niehoff
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.smack.spark.solution

import com.datastax.spark.connector.streaming._
import com.fasterxml.jackson.databind.ObjectMapper
import de.codecentric.smack.spark.model.Model
import de.codecentric.smack.spark.model.Model.{ Artist, Track, TrackByArtist }
import kafka.serializer.StringDecoder
import org.apache.log4j.{ Level, Logger }
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{ Seconds, StreamingContext }

import scala.collection.JavaConversions.asScalaBuffer

/**
 * Created by matthiasniehoff on 24.09.16.
 */
object SparkStreamingDownloadAnalytics {
  def main(args: Array[String]): Unit = {

    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    Logger.getLogger("com.datastax").setLevel(Level.WARN)
    Logger.getLogger("kafka.utils").setLevel(Level.WARN)

    val mapper: ObjectMapper = new ObjectMapper

    val conf = new SparkConf()
      .setAppName("Kafka Billboard Charts")
      .setMaster("local[*]")
//      .set("spark.cassandra.connection.host", "node-0.cassandra.mesos,node-1.cassandra.mesos,node-2.cassandra.mesos")
            .set("spark.cassandra.connection.host", "localhost")
      .set("spark.cassandra.connection.keep_alive_ms", "10000")
    val ssc = new StreamingContext(conf, Seconds(1))

    val topicsSet = Set("spotify")
    val kafkaParams = Map[String, String]("metadata.broker.list" -> "localhost:9092")
//    val kafkaParams = Map[String, String]("metadata.broker.list" -> "broker-0.kafka.mesos:9092")
    val stream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)

    // parsen
    val parsedStream = stream.map(s => {
      parseJson(mapper, s._2)
    }).cache()

    // ausgeben
    parsedStream.print()

    // zählen nach Album
    val countByAlbum = parsedStream.map(track => (track.album_name, 1)).reduceByKey(_ + _)
    countByAlbum.print()

    // alle Tracks eines Artist
    val tracksByArtist = parsedStream.flatMap(track => track.artists.map(artist => (artist, track))).groupByKey().mapValues(_.toStream.distinct.toSet)
    tracksByArtist.print()

    // speichern nach Cassandra
    // zuerst das set flach klopfen
    val oneLinePerTrackAndEntry: DStream[(Artist, Track)] = tracksByArtist.flatMapValues(set => set)
    // anschließend auf das korrekte Format für Cassandra bringen und speichern.
    oneLinePerTrackAndEntry.map {
      case (artist, track) => TrackByArtist(artist.name, track.id, track.disc_number, track.duration_ms, track.explicit, track.name, track.popularity, track.track_number)
    }.saveToCassandra("music", "tracks_by_artist")
    ssc.start()
    ssc.awaitTermination()

  }

  def parseJson(mapper: ObjectMapper, s: String): Model.Track = {
    val json = mapper.readTree(s)
    Track(
      json.path("album_name").asText,
      asScalaBuffer(json.path("artists").findValues("name")).map(node => Artist(node.asText())).toList,
      json.path("disc_number").asDouble,
      json.path("duration_ms").asDouble,
      json.path("explicit").asBoolean,
      json.path("id").asText,
      json.path("is_playable").asBoolean,
      json.path("name").asText,
      json.path("popularity").asDouble,
      json.path("track_number").asDouble
    )
  }
}
