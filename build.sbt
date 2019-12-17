import sbt.Keys.mainClass

name := "discourse-from-twitter"
packageName in Docker := "chrisbeach/discourse-from-twitter"
version := "0.2"
scalaVersion := "2.12.10"

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.2",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.danielasfregola" %% "twitter4s" % "6.2",
  "org.typelevel" %% "cats-effect" % "2.0.0"
)

mainClass in (Compile, run) := Some("com.brightercode.discoursefromtwitter.Runner")

lazy val discourse = RootProject(file("discourse-scala-client"))

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .dependsOn(discourse)
  .aggregate(discourse)

logBuffered in Test := false