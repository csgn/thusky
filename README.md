# Thusky

<img src="/docs/icon.jpg" width="256" height="256" />

Thusky is a scheduling library on Scala. In Thusky, all jobs have a lifecyle 
and retry policies. The jobs are working deterministically which means they 
can be re-runnable repeatedly.

# Create Task
```scala
import scala.concurrent.duration._

import cats._
import cats.syntax.all._
import cats.effect._

import thusky.core._

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    for {
      t1 <- Task.io[Int, Int](
        onAction = (i: Int) => {
          IO.sleep(2.seconds) *> IO(i + 1)
        },
        onError = err => IO.unit,
        onSucceed = res => IO.println("DONE"),
        onCancel = IO.unit
      )

      // ready
      _ <- t1.status.get
      _ <- IO.sleep(5.seconds)
      fiber <- t1.run(1).value.start
      // running
      _ <- t1.status.get
      _ <- IO.sleep(1.seconds)
      // succeed
      _ <- t1.status.get
    } yield ExitCode.Success
  }
}

```
