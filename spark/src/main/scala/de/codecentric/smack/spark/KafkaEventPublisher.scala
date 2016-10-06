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

import java.io.InputStream

import org.apache.kafka.clients.producer._
import java.util.HashMap

import scala.io.Source
import scala.util.Random

/**
 *
 */
object KafkaEventPublisher {
  def main(args: Array[String]) {

    val stream = getClass.getResourceAsStream("/test.json")
    val trackFile = Source.fromInputStream(stream)
    val tracks = trackFile.getLines().toVector
    trackFile.close()

    // Zookeeper connection properties
    val props = new HashMap[String, Object]()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
      "org.apache.kafka.common.serialization.StringSerializer"
    )
    props.put(
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
      "org.apache.kafka.common.serialization.StringSerializer"
    )
    val topic = "tracks"

    val producer = new KafkaProducer[String, String](props)

    // Send some messages
    while (true) {
      val random = Random.shuffle(tracks).take(50)
      random
        .foreach { str =>
          val message = new ProducerRecord[String, String](topic, null, str)
          producer.send(message)
        }

      Thread.sleep(200)
      System.out.println("Sent 50 Events to Kafka topic " + topic)
    }
  }
}
