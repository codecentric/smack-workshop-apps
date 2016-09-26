import sbt.Keys._

enablePlugins(JavaAppPackaging)

name := "smack-workshop-ingest"

organization := "de.codecentric.smack"

version := "0.1.0-SNAPSHOT"


val akkaVer        = "2.4.10"
val scalaVer       = "2.11.8"
val scalaParsersVer= "1.0.4"
val scalaTestVer   = "2.2.6"
val Log4j2         = "2.5"
val Slf4j          = "1.7.18"
val kafkaVersion   = "0.10.0.1"
val confluentKafka = "3.0.1"
val akkaStreamKafka = "0.11-RC2"

scalaVersion := scalaVer

resolvers += "confluent" at "http://packages.confluent.io/maven/"


// For Settings/Task reference, see http://www.scala-sbt.org/release/sxr/sbt/Keys.scala.html

lazy val compileOptions = Seq(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-Xcheckinit"
)

//noinspection ScalaStyle
lazy val commonDependencies = Seq(
  "org.scalatest"            %% "scalatest"                  % scalaTestVer       % "test",
  //"com.esotericsoftware.kryo" % "kryo" % "2.24.0",
  "com.twitter"              %% "chill-akka"                 % "0.8.0"
).map(_. excludeAll(
  ExclusionRule(organization = "org.slf4j", artifact = "log4j-over-slf4j"),
  ExclusionRule(artifact = "slf4j-log4j12"),
  ExclusionRule(organization = "com.sun.jdmk"),
  ExclusionRule(organization = "com.sun.jmx"),
  ExclusionRule(organization = "log4j"),
  ExclusionRule(organization = "javax.jms")
))

//noinspection ScalaStyle
lazy val akkaDependencies = Seq(
  "org.scala-lang.modules"   %% "scala-parser-combinators"   % scalaParsersVer,
  "com.typesafe.akka"        %% "akka-actor"                 % akkaVer,
  "com.typesafe.akka"        %% "akka-slf4j"                 % akkaVer,
  "com.typesafe.akka"        %% "akka-testkit"               % akkaVer            % "test",
  // these are to avoid sbt warnings about transitive dependency conflicts
  "com.typesafe.akka"               %% "akka-stream-kafka"          % akkaStreamKafka,
  //"org.apache.kafka"                %% "kafka"                      % kafkaVersion,
  "io.confluent"                    % "kafka-avro-serializer"       % confluentKafka
).map(_. excludeAll(
  ExclusionRule(organization = "org.slf4j", artifact = "log4j-over-slf4j"),
  ExclusionRule(artifact = "slf4j-log4j12"),
  ExclusionRule(organization = "com.sun.jdmk"),
  ExclusionRule(organization = "com.sun.jmx"),
  ExclusionRule(organization = "log4j"),
  ExclusionRule(organization = "javax.jms")
))

//noinspection ScalaStyle
lazy val logDependencies = Seq(
  "org.slf4j"                       % "slf4j-api"                           % Slf4j,
  "org.apache.logging.log4j"        % "log4j-1.2-api"                       % Log4j2,
  //slf4j complains about 2 log4j bindings in classpath without this commented out
  //"org.apache.logging.log4j"        % "log4j-slf4j-impl"                    % Log4j2,
  "org.apache.logging.log4j"        % "log4j-api"                           % Log4j2,
  "org.apache.logging.log4j"        % "log4j-core"                          % Log4j2,
  "org.slf4j"                       % "jcl-over-slf4j"                      % Slf4j,
  "org.slf4j"                       % "jul-to-slf4j"                        % Slf4j
).map(_. excludeAll(
  ExclusionRule("org.slf4j", "log4j-over-slf4j")
))

lazy val testDependencies = Seq (
  "org.apache.kafka"                %% "kafka"                      % kafkaVersion        % "test" classifier "test",
  "org.apache.kafka"                % "kafka-clients"               % kafkaVersion        % "test" classifier "test",
  "io.confluent"                    % "kafka-schema-registry"       % confluentKafka      % "test"
).map(_. excludeAll(
  ExclusionRule("org.slf4j", "log4j-over-slf4j"),
  ExclusionRule(organization = "com.sun.jdmk"),
  ExclusionRule(organization = "com.sun.jmx"),
  ExclusionRule(organization = "log4j"),
  ExclusionRule(organization = "javax.jms")
))


scalacOptions ++= compileOptions

libraryDependencies ++= commonDependencies
libraryDependencies ++= akkaDependencies
libraryDependencies ++= testDependencies
libraryDependencies ++= logDependencies





addCommandAlias("createAll", ";clean ;assembly ;docker:publishLocal")


