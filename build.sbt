name := "netflix"

version := "1.0"

lazy val `netflix` = (project in file(".")).enablePlugins(PlayJava).enablePlugins(PlayEbean)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
    javaJdbc,
    cache,
    javaWs
)


fork in run := true