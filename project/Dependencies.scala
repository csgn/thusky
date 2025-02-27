import sbt._

object Dependencies {
  object V {
    val scalactic = "3.2.19"
    val scalatest = "3.2.19"
    val cats = "2.12.0"
    val catsEffect = "3.5.7"
    val catsEffectTestingScalatest = "1.6.0"
  }

  val scalactic = "org.scalactic" %% "scalactic" % V.scalactic
  val scalatest = "org.scalatest" %% "scalatest" % V.scalatest % Test
  val cats = "org.typelevel" %% "cats-core" % V.cats
  val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect
  val catsEffectTestingScalatest =
    "org.typelevel" %% "cats-effect-testing-scalatest" % V.catsEffectTestingScalatest % Test
}
