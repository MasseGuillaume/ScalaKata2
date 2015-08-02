addSbtPlugin("com.scalakata" % "sbt-scalakata" % "1.0.3")
resolvers += Resolver.bintrayRepo("masseguillaume", "sbt-plugins") 

resolvers += Resolver.url(
  "masseguillaume/sbt-plugins",
  url("http://dl.bintray.com/masseguillaume/sbt-plugins/")
)(Resolver.ivyStylePatterns)