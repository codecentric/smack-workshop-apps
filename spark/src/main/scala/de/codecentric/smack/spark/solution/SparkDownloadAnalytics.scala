package de.codecentric.smack.spark.solution

import com.datastax.spark.connector._
import com.fasterxml.jackson.databind.ObjectMapper
import de.codecentric.smack.spark.model.Model.TrackByArtist
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

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
      .setMaster("local[2]")
      //      .set("spark.cassandra.connection.host", "node-0.cassandra.mesos,node-1.cassandra.mesos,node-2.cassandra.mesos")
      .set("spark.cassandra.connection.host", "localhost")
      .set("spark.cassandra.connection.keep_alive_ms", "10000")

    val sc = new SparkContext(conf)
    val tracksByArtist = sc.cassandraTable[TrackByArtist]("music","tracks_by_artist")
    tracksByArtist.foreach(println)
    val popularTracks = tracksByArtist.filter(_.popularity > 70.0)
    popularTracks.foreach(println)
  }

}
