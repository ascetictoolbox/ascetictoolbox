name := """application-monitor"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

resolvers += "MongoAL at GitHub" at "https://raw.githubusercontent.com/mariomac/MongoAL/master/mvn-repo"


libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "org.mongodb" % "mongo-java-driver" % "[2.12.2,)",
  "es.bsc" % "mongoal" % "0.1"
)
