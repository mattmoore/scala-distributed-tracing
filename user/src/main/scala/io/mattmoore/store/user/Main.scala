package io.mattmoore.store.user

import cats.effect._
import doobie.Transactor
import io.mattmoore.store.user.algebras._
import io.mattmoore.store.user.domain._
import io.mattmoore.store.user.database._
import io.mattmoore.store.user.services._

object Main extends IOApp {
  type F[A] = IO[A]

  override def run(args: List[String]): IO[ExitCode] = {
    val xa: Transactor[F] = Transactor.fromDriverManager[F](
      "org.postgresql.Driver",
      "jdbc:postgresql:user",
      "postgres",
      ""
    )
    val db: Repository[F, User] = new UserRepository(xa)
    val userService: UserServiceAlgebra[F] = new UserService[F](db)

    IO(ExitCode.Success)
  }
}
