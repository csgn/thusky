package thusky.core
package test

import cats.syntax.all._
import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

class TaskSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  "Task" - {
    "should complete succeeded" in {
      for {
        t1 <- Task[Int]("t1") {
          IO(1 + 1)
        }
        completed <- t1.start.flatMap(_.await)
        _ = completed.exitReason.shouldBe(Task.Succeeded(IO.pure(2)))
      } yield ()
    }

    "should complete failed" in {
      val exception = new Exception("error")

      for {
        t1 <- Task[Int]("t1") {
          IO.raiseError(exception)
        }
        completed <- t1.start.flatMap(_.await)
        _ = completed.exitReason.shouldBe(Task.Failed(exception))
      } yield ()
    }

    "should complete canceled" in {
      for {
        t1 <- Task("t1") {
          IO.sleep(5.seconds) *> IO(1 + 1)
        }

        completed <- t1.start.flatMap(a => {
          (a.await, a.fiber.cancel).parTupled.map { case (a, _) =>
            a
          }
        })

        _ = completed.exitReason.shouldBe(Task.Canceled)
      } yield ()
    }

  }
}
