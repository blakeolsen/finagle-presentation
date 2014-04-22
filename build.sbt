name := "finagle-prez"

scalaVersion := "2.10.3"

fork in run := true

cancelable in run := true

resolvers += "hopper-repo" at "http://nexus.lab.mtl/nexus/content/repositories/releases/"

resolvers += "twttr" at "http://maven.twttr.com/"

libraryDependencies += "com.twitter" %% "finagle-http" % "6.13.1"

libraryDependencies += "com.twitter" %% "finagle-redis" % "6.13.1"

libraryDependencies += "com.twitter" %% "finagle-stats" % "6.13.1"

libraryDependencies += "com.twitter" %% "finagle-zipkin" % "6.13.1"

libraryDependencies += "com.twitter" %% "finagle-serversets" % "6.13.1"

libraryDependencies += "com.twitter" %% "twitter-server" % "1.6.1"
