lazy val smackWorkshop = project
  .copy(id = "smack-workshop")
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin, GitVersioning)

name := "smack-workshop"

version := "1.0.0"

libraryDependencies ++= Vector(
  Library.spark,
  Library.sparkKafka,
  Library.sparkSql,
  Library.sparkStreaming,
  Library.sparkCassandra
)

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.4.4"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.4"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-annotations" % "2.4.4"

initialCommands := """|import de.codecentric.smack._
                      |""".stripMargin


assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("META-INF", "ECLIPSEF.RSA") => MergeStrategy.discard
  case PathList("META-INF", "DUMMY.RSA") => MergeStrategy.discard
  case PathList("META-INF", "ECLIPSEF.SF") => MergeStrategy.discard
  case PathList("META-INF", "DUMMY.SF") => MergeStrategy.discard
  case _ => MergeStrategy.first
}

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

scalaVersion := Version.Scala
