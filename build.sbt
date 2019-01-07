name := "workshop-akka-typed"
organization := "typesafe-utrecht"

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.5.19"
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j"       % "2.5.19"
libraryDependencies += "ch.qos.logback"    % "logback-classic"   % "1.2.3"

scalafmtOnCompile in ThisBuild := true

enablePlugins(GitVersioning)
