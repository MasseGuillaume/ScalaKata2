lazy val scalakata = (project in file(".")).
  enablePlugins(ScalaKataPlugin).
  settings(
    organization := "masseguillaume",
    name := "scalakata",
    version := "1.0.0",
    description := "Docker Container with various librairies",
    scalaVersion := "2.11.7"
  )