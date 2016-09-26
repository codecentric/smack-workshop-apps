import sbt._

object Version {
  final val Scala      = "2.10.6"
  final val Spark      = "1.6.2"
  final val SparkCassandra = "1.6.2"
}

object Library {
  val spark           = "org.apache.spark"    %% "spark-core"                 % Version.Spark
  val sparkStreaming  = "org.apache.spark"    %% "spark-streaming"            % Version.Spark
  val sparkSql        = "org.apache.spark"    %% "spark-sql"                  % Version.Spark
  val sparkKafka      = "org.apache.spark"    %% "spark-streaming-kafka"      % Version.Spark
  val sparkCassandra  = "com.datastax.spark"  %% "spark-cassandra-connector"  % Version.SparkCassandra
}
