ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"


lazy val scalacopts = Seq(
  "-feature",
  "-deprecation",
  "-encoding","UTF-8",
  "-language:postfixOps",
  "-language:higherKinds",
  "-Wunused:imports",
  "-Ymacro-annotations"
)

lazy val root = (project in file("."))
  .settings(
    name := "zio-fashion",
      scalacOptions ++= scalacopts
  )

val ZIOVersion  = "2.0.11"
libraryDependencies ++= Seq(
  "dev.zio"    %% "zio"                 % ZIOVersion,
  "dev.zio" %% "zio-streams" %  ZIOVersion,
  "dev.zio" %% "zio-http" % "3.0.0-RC2",
  "io.circe" %% "circe-core" % "0.14.2",
  "io.circe" %% "circe-generic" % "0.14.2",
  "io.circe" %% "circe-parser" % "0.14.2"
  )


