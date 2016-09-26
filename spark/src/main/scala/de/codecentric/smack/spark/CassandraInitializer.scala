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
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.log4j.{ Level, Logger }
import org.apache.spark._

object CassandraInitializer {
  def main(args: Array[String]) {

    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    Logger.getLogger("com.datastax").setLevel(Level.WARN)

    if (args.length < 1) {
      System.err.println(
        s"""
           |Usage: CassandraInitializer <cassandraHost>
           |  <cassandraHost> is a list of one ore more Cassandra nodes
           |
        """.stripMargin
      )
      System.exit(1)
    }

    val Array(cassandraHosts) = args

    val conf = new SparkConf()
      .setAppName("Load Album Information to Cassandra")
      .setMaster("local")
      .set("spark.cassandra.connection.host", cassandraHosts)
    val sc = new SparkContext(conf)

    val cassandraConnector = CassandraConnector.apply(conf)

    cassandraConnector.withSessionDo(session => {
      session.execute("CREATE KEYSPACE IF NOT EXISTS music WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'} ")
      session.execute("CREATE TABLE IF NOT EXISTS music.albums (title text,year int,genre text,performer text,PRIMARY KEY ((title, year)));")
      session.execute("CREATE TABLE IF NOT EXISTS music.billboard_charts (title text, year int, count bigint,PRIMARY KEY ((title, year)));")
      session.execute("CREATE TABLE IF NOT EXISTS music.charts_with_diff_state (title text,year int,count bigint, PRIMARY KEY ((title, year)));")
      session.execute("CREATE TABLE IF NOT EXISTS music.charts_with_complete_state (title text,year int,count bigint, genre text, performer text, PRIMARY KEY ((title, year)));")
    })

    val file = sc.textFile(getClass.getResource("/albums.csv").toString)
    file.map(_.split(",")).map {
      case Array(title, year, performer, genre) => Album(title, year.toInt, genre, performer)
      case _                                    => throw new RuntimeException("Invalid format");
    }.saveToCassandra("music", "albums")

    System.out.println("==================================================================================")
    System.out.println("Created Keyspace 'music' with tables and loaded data to table 'albums'")
    System.out.println("==================================================================================")

    sc.stop()
  }
}

case class Album(title: String, year: Int, genre: String, performer: String)