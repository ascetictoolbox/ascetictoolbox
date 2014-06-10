name := "AppMonAPI"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "org.mongodb" % "mongo-java-driver" % "[2.12.2,)"
  //"org.joda" % "joda-time" % "[2.3,)"
)

play.Project.playJavaSettings
