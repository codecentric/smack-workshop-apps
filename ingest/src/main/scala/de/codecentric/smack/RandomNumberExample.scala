package de.codecentric.smack

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.{Done, NotUsed}
import com.lambdaworks.jacks.JacksMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.concurrent.Promise
import scala.util.Random

object RandomNumberExample {
  def main(args: Array[String]): Unit = {


    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    import system.dispatcher

    val source = Source((10 to 20))
    val randomizer = Flow[Int].map(Random.nextInt(_))
    val sink = Sink.foreach[Int](println _)

    //source.runWith(sink)


   source.via(randomizer).runWith(sink)







  }
}
