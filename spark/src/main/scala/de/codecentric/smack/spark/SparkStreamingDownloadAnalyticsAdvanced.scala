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
import de.codecentric.smack.spark.model.Model.{ Artist, Track }
import kafka.serializer.StringDecoder
import org.apache.log4j.{ Level, Logger }
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
object SparkStreamingDownloadAnalyticsAdvanced {
  def main(args: Array[String]): Unit = {

    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    Logger.getLogger("com.datastax").setLevel(Level.WARN)
    Logger.getLogger("kafka.utils").setLevel(Level.WARN)

    val mapper: ObjectMapper = new ObjectMapper

    val conf = new SparkConf()
      .setAppName("Kafka Billboard Charts")
      .setMaster("local[*]")
    val ssc = new StreamingContext(conf, Seconds(1))

    val topicsSet = Set("spotify")
    val kafkaParams = Map[String, String]("metadata.broker.list" -> "broker-0.kafka.mesos:9092")
    //    val kafkaParams = Map[String, String]("metadata.broker.list" -> "localhost:9092") // for local
    val stream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)

    // parsen

    // alle einträge mit der gleichen ID die innerhalb von 2sekunden ankommen zusammen fassen und zählen. Dies jede Minute durchführen.

    // State definieren mit der State Spec funktion
    val billboardState = StateSpec.function(updateBillboardState _)

    // Stream mit State mappen

    // Stream ggfs formatieren und ausgeben

    ssc.checkpoint("/tmp")
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

  /**
   * Verwendet, um die aktuellen Downloadzahlen mit den vorherigen, gespeichert im State, zu vergleichen und die Änderung zurück zu geben.
   * @param batchTime - Zeit des Batches, nicht relevant
   * @param key - Der Key für den State, hier die TrackID
   * @param value - Der neue Value, hier der Download Count
   * @param state - Der alte Value im State, hier (Count,Veränderung)
   * @return Ein Tupel bestehend aus dem Key und dem Count sowie der neuen Veränderung
   */
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
