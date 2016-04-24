# ScalaKata

[![Stories in Ready](https://img.shields.io/waffle/label/MasseGuillaume/ScalaKata2.svg?style=flat-square)](https://waffle.io/MasseGuillaume/ScalaKata2) 
[![Travis Build Status](https://img.shields.io/travis/MasseGuillaume/ScalaKata2.svg?style=flat-square)](https://travis-ci.org/MasseGuillaume/ScalaKata2) 
[![Windows Build status](https://img.shields.io/appveyor/ci/MasseGuillaume/ScalaKata2.svg?style=flat-square)](https://ci.appveyor.com/project/MasseGuillaume/scalakata2/branch/master) 
[![Chat on Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/MasseGuillaume/ScalaKata2) 

![Demo](/misc/demo.gif)

## Distributions

### Sbt Plugin

Add the following line to `project/plugins.sbt`

```scala
addSbtPlugin("com.scalakata" % "sbt-scalakata" % "1.1.3")
```

And add the following line to `build.sbt`

```scala
enablePlugins(ScalaKataPlugin)
```

### Docker container

`sudo docker run -p 7331:7331 --name scalakata masseguillaume/scalakata:v1.1.3`

open your browser at `http://localhost:7331`
