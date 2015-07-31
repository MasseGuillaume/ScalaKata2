lazy val test = (project in file(".")).
  enablePlugins(ScalaKataPlugin).
  settings(
    name := "scalakata",
    version := "1.0.0",
    scalaVersion := "2.11.7",
    libraryDependencies += "org.spire-math" %% "cats" % "0.1.2"
  )
