package thusky.core

import cats._
import cats.syntax.all._
import cats.effect._
import cats.effect.kernel.Outcome

import java.util.UUID

sealed trait Task
object Task {
  case class Scheduled[A](
      id: String,
      private val effect: IO[A]
  ) extends Task {
    def start: IO[Running[A]] = for {
      exitReason <- Deferred[IO, ExitReason[Throwable]]
      fiber <- effect
        .guaranteeCase(o =>
          o match {
            case Outcome.Errored(e)   => exitReason.complete(Failed(e)).void
            case Outcome.Canceled()   => exitReason.complete(Canceled).void
            case Outcome.Succeeded(a) => exitReason.complete(Succeeded(a)).void
          }
        )
        .attempt
        .start
    } yield Running(id, fiber, exitReason)
  }

  case class Running[A](
      id: String,
      val fiber: FiberIO[Either[Throwable, A]],
      private val exitReason: Deferred[IO, ExitReason[Throwable]]
  ) extends Task {
    def await: IO[Completed] = for {
      exitReasonValue <- exitReason.get
    } yield Completed(id, exitReasonValue)
  }

  case class Completed(id: String, exitReason: ExitReason[Throwable]) extends Task

  sealed trait ExitReason[+E]
  case class Succeeded[A](value: A) extends ExitReason[Nothing]
  case class Failed[E](e: E) extends ExitReason[E]
  case object Canceled extends ExitReason[Nothing]

  def apply[A](id: String)(effect: IO[A]): IO[Scheduled[A]] = IO {
    Scheduled(id, effect)
  }
}
