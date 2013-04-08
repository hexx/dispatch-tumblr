organization := "com.github.hexx"

name := "dispatch-tumblr"

version := "0.0.1"

scalaVersion := "2.10.1"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

libraryDependencies ++= Seq(
  "net.databinder" %% "dispatch-http-json" % "0.8.9",
  "net.databinder" %% "dispatch-oauth" % "0.8.9",
  "org.specs2" %% "specs2" % "1.14" % "test"
)
