name := """application-monitor"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"


libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "org.jongo" % "jongo" % "1.2",
  "org.antlr" % "antlr4" % "4.3",
  "org.mongodb" % "mongo-java-driver" % "3.0.1",
  "org.apache.qpid" % "qpid-amqp-1-0-client-jms" % "0.32",
  "org.apache.qpid" % "qpid-amqp-1-0-client" % "0.32",
  "org.apache.qpid" % "qpid-amqp-1-0-common" % "0.32",
  "org.apache.geronimo.specs" % "geronimo-jms_1.1_spec" % "[1.1.1,)"
)
