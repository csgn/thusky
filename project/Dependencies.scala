import sbt._

object Dependencies {
  object V {
    val munit      = "1.0.0-M10"
    val cats       = "2.12.0"
    val catsEffect = "3.5.7"
  }

  val munit      = "org.scalameta" %% "munit"       % V.munit % Test
  val cats       = "org.typelevel" %% "cats-core"   % V.cats
  val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect
}

