name := "workshop-akka-typed"
organization := "typesafe-utrecht"

val akkaVersion = "2.5.19"
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j"               % akkaVersion
libraryDependencies += "ch.qos.logback"    % "logback-classic"           % "1.2.3"
libraryDependencies += "org.scalatest"     %% "scalatest"                % "3.0.5" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-testkit"             % akkaVersion % Test

scalafmtOnCompile in ThisBuild := true

enablePlugins(GitVersioning)
