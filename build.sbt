organization  := "com.example"

version       := "0.1"

scalaVersion  := "2.11.6"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "io.spray"                %%  "spray-can"     % sprayV,
    "io.spray"                %%  "spray-routing" % sprayV,
    "io.spray"                %% "spray-json"     % "1.3.2",
    "org.json4s"              %% "json4s-native"  % "3.2.10",
    "org.json4s"              %% "json4s-ext"     % "3.2.10",
    "com.typesafe.akka"       % "akka-slf4j_2.11" % "2.3.9",
    "io.spray"                %%  "spray-testkit" % sprayV    % "test",
    "com.typesafe.akka"       %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"       %%  "akka-testkit"  % akkaV     % "test",
    "org.specs2"              %%  "specs2-core"   % "2.3.11"  % "test"
  )
}

libraryDependencies += "net.sf.uadetector" % "uadetector-resources" % "2014.10"

Revolver.settings
