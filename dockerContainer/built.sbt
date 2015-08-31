lazy val scalakata = (project in file(".")).
  enablePlugins(ScalaKataPlugin).
  settings(
    organization := "masseguillaume",
    name := "scalakata",
    version := "1.0.6",
    description := "Docker Container",
    scalaVersion := "2.11.7",
    securityManager in Backend := true
  )