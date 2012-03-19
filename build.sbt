scalaVersion := "2.9.1"

scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  "net.databinder" %% "dispatch-http-json" % "0.8.8",
  "net.databinder" %% "dispatch-oauth" % "0.8.8",
  "org.specs2" %% "specs2" % "1.8.2" % "test"
)
