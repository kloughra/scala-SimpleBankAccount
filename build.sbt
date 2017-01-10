name := "Bank_Account"

version := "0.0.1"

scalaVersion := "2.11.8"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.0"
libraryDependencies += "com.wix" %% "accord-core" % "0.4.2"
libraryDependencies += "com.wix" %% "accord-specs2" % "0.4.2"

//libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" %"2.3.12"
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" %"2.3.12"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.2" % "test"
//libraryDependencies += "org.specs2" %% "specs2-core" % "3.8.4" % "test"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-remote"  % "2.3.12",
  "com.typesafe.akka" %% "akka-agent"   % "2.3.12",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.12" % "test",
    "org.specs2" %% "specs2-core" % "3.8.4" % "test" exclude("org.scalaz", "scalaz-core_2.11"),
    "org.specs2" %% "specs2-scalacheck" % "3.8.4",
    "org.specs2" %% "specs2-mock" % "3.8.4" % "test",
    "org.specs2" %% "specs2-junit" % "3.8.4" % "test"
)
//scalacOptions in Test ++= Seq("-Yrangepos")

