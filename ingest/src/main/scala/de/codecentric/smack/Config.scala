package de.codecentric.smack

import com.typesafe.config.ConfigFactory

object Config {
  private val config =  ConfigFactory.load()


  lazy val kafkaConnectionString = config.getString("ingest.kafkaConnectionString")
  lazy val kafkaTopic = config.getString("ingest.kafkaTopic")
  lazy val websocketUrl = config.getString("ingest.websocketUrl")


}
