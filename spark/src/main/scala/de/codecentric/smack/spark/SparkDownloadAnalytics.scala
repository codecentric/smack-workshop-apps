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
import org.apache.log4j.{ Level, Logger }
import org.apache.spark.{ SparkConf, SparkContext }
import com.datastax.spark.connector._
import de.codecentric.smack.spark.model.Model.TrackByArtist

/**
 * Created by matthiasniehoff on 26.09.16.
 */
object SparkDownloadAnalytics {

  def main(args: Array[String]) {
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    Logger.getLogger("com.datastax").setLevel(Level.WARN)
    Logger.getLogger("kafka.utils").setLevel(Level.WARN)

    val mapper: ObjectMapper = new ObjectMapper

    val conf = new SparkConf()
      .setAppName("Kafka Billboard Charts")
      .setMaster("local[*]")
//            .set("spark.cassandra.connection.host", "node-0.cassandra.mesos,node-1.cassandra.mesos,node-2.cassandra.mesos")
      .set("spark.cassandra.connection.host", "localhost")
      .set("spark.cassandra.connection.keep_alive_ms", "10000")

    val sc = new SparkContext(conf)
    // read Table and print

    // get all tracks with popularity > 70

  }

}
