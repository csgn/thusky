import Dependencies._

/* Project settings */
ThisBuild / name := "Thusky"
ThisBuild / scalaVersion := "2.13.15"
ThisBuild / version := "1.0.0-SNAPSHOT"
ThisBuild / description := "Job scheduling and monitoring."
ThisBuild / licenses := List(("MIT", url("https://opensource.org/license/mit")))
ThisBuild / developers ++= List(
  // username -> ("Fullname", "email")
  "csgn" -> ("Sergen Cepoglu", "dev.csgn@gmail.com")
).map { case (username, (fullname, email)) =>
  Developer(
    id = username,
    name = fullname,
    email = email,
    url = url(s"https://github.com/$username"),
  )
}

ThisBuild / Compile / run / fork := true


lazy val core = project
  .in(file("modules/core"))
  .settings(
    name := "core",
    moduleName := "thusky-core",
  )
  .settings(
    libraryDependencies ++= {
      Seq(
        scalactic,
        scalatest,
        cats,
        catsEffect,
        catsEffectTestingScalatest,
      )
    }
  )
  .enablePlugins(ScalafixPlugin)

lazy val thusky = project
  .in(file("."))
  .settings(
    name := "thusky",
  )
  .aggregate(core)

