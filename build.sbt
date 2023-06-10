import Dependencies._

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.eozel"
ThisBuild / organizationName := "eozel"

ThisBuild / semanticdbEnabled := true


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
    name := "speech_challenge",
    libraryDependencies ++= Seq(
      zio,
      zioHttp,
      zioStream,
      scalaTest % Test
    ) ++ circe ++ zioTest,
    scalacOptions ++= scalacopts,
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )