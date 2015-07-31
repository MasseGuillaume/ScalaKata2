lazy val test = (project in file(".")).
  enablePlugins(ScalaKataPlugin).
  settings(
    scalaVersion := "2.11.7"
  )
