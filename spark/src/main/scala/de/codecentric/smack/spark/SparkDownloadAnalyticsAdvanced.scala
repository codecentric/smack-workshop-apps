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

import com.datastax.spark.connector._
import com.fasterxml.jackson.databind.ObjectMapper
import de.codecentric.smack.spark.model.Model
import de.codecentric.smack.spark.model.Model.{Artist, Track}
import kafka.serializer.StringDecoder
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming._
import com.datastax.spark.connector._
import com.datastax.spark.connector.streaming._

import scala.collection.JavaConversions.asScalaBuffer

/**
  * Created by matthiasniehoff on 24.09.16.
  */
object SparkDownloadAnalyticsAdvanced {
  def main(args: Array[String]): Unit = {

    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    Logger.getLogger("com.datastax").setLevel(Level.WARN)
    Logger.getLogger("kafka.utils").setLevel(Level.WARN)

    val mapper: ObjectMapper = new ObjectMapper

    val conf = new SparkConf()
      .setAppName("Kafka Billboard Charts")
      .setMaster("local[2]")
      //      .set("spark.cassandra.connection.host", "node-0.cassandra.mesos,node-1.cassandra.mesos,node-2.cassandra.mesos")
      .set("spark.cassandra.connection.host", "localhost")
      .set("spark.cassandra.connection.keep_alive_ms", "10000")
    val ssc = new StreamingContext(conf, Seconds(1))

    val topicsSet = Set("topic")
    val kafkaParams = Map[String, String]("metadata.broker.list" -> "localhost:9092")
    val stream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)

    val file = ssc.sparkContext.textFile("src/main/resources/test.json")

    // parsen
    val parsedStream = stream.map(s => {
      parseJson(mapper, s._2)
    }).cache()

    // ausgeben
    parsedStream.print(10)
    val billboardState = StateSpec.function(updateBillboardState _)


    // Reduce By Window
    val windowedStream = parsedStream.map(track => (track.id, 1))
      .reduceByKeyAndWindow(
        { (countA, countB) => countA + countB }, { (countA, countB) => countA - countB },
        Seconds(2), // Window Duration
        Seconds(1)
      ) // Slide Duration
    // process the last 60s every 30s

    // update the stream and the state
    // as the updatedStream is used for multiple actions (saveToCassandra, print) it should be cached to prevent recomputation.
    val updatedStream = windowedStream.mapWithState(billboardState).cache()

    //
    //      // save updated stream to cassandra
    //      updatedStream
    //        .map { case ((title, year), (count, _)) => (title, year, count) }
    //        .saveToCassandra("music", "billboard_charts")
    //
    //      // join with additional data from cassandra
    val joinedStream = updatedStream
      .map { case ((title, year), (count, percentage)) => (title, year, count, percentage) }
      .joinWithCassandraTable[(String, String)]("music", "albums", selectedColumns = SomeColumns("performer", "genre"))
    //
    //      // Sort and print to console with additional/joined information
    joinedStream.transform(rdd =>
      rdd.map { case ((title, year, count, percentage), (performer, genre)) => (count, (percentage, title, year, performer, genre)) }
        .sortByKey(false)
        .map { case (count, (percentage, title, year, performer, genre)) => (count, "%+.2f%%".format(percentage).replace("NaN%", "NEW!"), title, year, performer, genre) })
      .print(10)

    ssc.checkpoint("/tmp")

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

  def updateBillboardState(batchTime: Time, key: String, value: Option[Int], state: State[(Long, Double)]): Option[(String, (Long, Double))] = {
    var billboard = (value.getOrElse(0).toLong, Double.NaN)
    if (state.exists && state.get._1 != 0) {
      val change = (billboard._1 - state.get._1) * 100 / state.get._1
      billboard = (value.getOrElse(0).toLong, change)
    }
    state.update(billboard)
    Some(key, billboard)
  }
}
