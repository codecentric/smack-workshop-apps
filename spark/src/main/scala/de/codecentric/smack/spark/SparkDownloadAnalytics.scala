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

package de.codecentric.smack.spark

import com.fasterxml.jackson.databind.ObjectMapper
import de.codecentric.smack.spark.model.Model
import de.codecentric.smack.spark.model.Model.{Artist, Track}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.JavaConversions.asScalaBuffer

/**
  * Created by matthiasniehoff on 24.09.16.
  */
object SparkDownloadAnalytics {
  def main(args: Array[String]): Unit = {

    val mapper: ObjectMapper = new ObjectMapper

    val conf = new SparkConf()
      .setAppName("Kafka Billboard Charts")
      .setMaster("local[2]")
    val ssc = new StreamingContext(conf, Seconds(1))

    //    // Read from Kafka
    //    val kafkaParams = Map[String, Object](
    //      "bootstrap.servers" -> "localhost:9092",
    //      "key.deserializer" -> classOf[StringDeserializer],
    //      "value.deserializer" -> classOf[StringDeserializer],
    //      "group.id" -> "SparkDownloadAnalytics",
    //      "auto.offset.reset" -> "latest",
    //      "enable.auto.commit" -> false
    //    )
    //
    //    val stream = KafkaUtils.createDirectStream[String, String](
    //      ssc,
    //      PreferConsistent,
    //      Subscribe[String, String](Set("someTopic"), kafkaParams)
    //    )

    val file = ssc.sparkContext.textFile("/Users/matthiasniehoff/dev/repos/git/smack-workshop/src/main/resources/test.json")

    val parsedStream = file.map(s => {
      parseJson(mapper, s)
    })

    parsedStream.collect()

    val countByAlbum: RDD[(String, Int)] = parsedStream.map(track => (track.album_name, 1)).reduceByKey(_ + _)
    val allTrackByArtist: RDD[(Artist, Iterable[Track])] = parsedStream.flatMap(track => track.artists.map(artist => (artist, track))).groupByKey()

    // count
    // count by key
    // window
    // save to cassandra
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
