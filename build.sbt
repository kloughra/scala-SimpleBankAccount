name := "Bank_Account"

version := "0.0.1"

scalaVersion := "2.11.8"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" %"2.3.12"
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" %"2.3.12"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-remote"  % "2.3.12",
  "com.typesafe.akka" %% "akka-agent"   % "2.3.12",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.12" % "test"
)
