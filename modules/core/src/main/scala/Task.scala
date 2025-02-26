package thusky.core

import cats._
import cats.data._
import cats.syntax.all._
import cats.effect._
import cats.effect.syntax.all._

sealed trait Task[F[_], T, R] {
  val status: Ref[F, Task.Status]

  // lifecycle methods
  protected def _onAction: T => F[R]
  protected def _onSucceed: R => F[Unit]
  protected def _onError: Throwable => F[Unit]
  protected def _onSuspend: F[Unit]
  protected def _onCanceled: F[Unit]

  protected def action: T => F[R]
  protected def error: Throwable => F[Unit]
  protected def succeed: R => F[Unit]
  protected def suspend: F[Unit]
  protected def cancel: F[Unit]

  def run(t: => T): EitherT[F, Throwable, R]

  override def toString(): String = s"Task[${status}]"
}

object Task {

  def of[F[_]: MonadCancelThrow: Async, T, R](
      onAction: T => F[R],
      onSucceed: R => F[Unit],
      onError: Throwable => F[Unit],
      onCancel: => F[Unit],
      onSuspend: => F[Unit]
  ): F[Task[F, T, R]] = for {
    ref <- Ref.of[F, Status](Status.Ready)
  } yield new Task[F, T, R] {
    val status: Ref[F, Status] = ref

    protected def _onAction: T => F[R] = onAction
    protected def _onError: Throwable => F[Unit] = onError
    protected def _onSucceed: R => F[Unit] = onSucceed
    protected def _onSuspend: F[Unit] = onSuspend
    protected def _onCanceled: F[Unit] = onCancel

    protected def action: T => F[R] = status.set(Status.Running) *> _onAction(_)
    protected def error: Throwable => F[Unit] = status.set(Status.Failed) *> _onError(_)
    protected def succeed: R => F[Unit] = status.set(Status.Succeed) *> _onSucceed(_)
    protected def suspend: F[Unit] = status.set(Status.Suspended) *> _onSuspend
    protected def cancel: F[Unit] = status.set(Status.Canceled) *> _onCanceled

    def run(t: => T): EitherT[F, Throwable, R] = {
      EitherT(action(t).attempt.onCancel(cancel))
        .semiflatTap(succeed)
        .leftSemiflatTap(error)
    }
  }

  def io[T, R](
      onAction: T => IO[R],
      onSucceed: R => IO[Unit] = (_: R) => IO.unit,
      onError: Throwable => IO[Unit] = (_: Throwable) => IO.unit,
      onCancel: => IO[Unit] = IO.unit,
      onSuspend: => IO[Unit] = IO.unit
  ): IO[Task[IO, T, R]] = of[IO, T, R](
    onAction = onAction,
    onSucceed = onSucceed,
    onError = onError,
    onCancel = onCancel,
    onSuspend = onSuspend
  )

  sealed trait Status
  object Status {
    case object Ready extends Status
    case object Running extends Status
    case object Suspended extends Status
    case object Failed extends Status
    case object Succeed extends Status
    case object Canceled extends Status
  }
}
