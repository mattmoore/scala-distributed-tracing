package io.mattmoore.store.user

import cats.effect._
import doobie._
import io.mattmoore.store.user.algebras._
import io.mattmoore.store.user.domain._
import io.mattmoore.store.user.repositories._
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
    val userRepository: Repository[F, User] = new UserRepository(xa)
    val userService: UserService[F] = new UserServiceInterpreter[F](userRepository)

    IO(ExitCode.Success)
  }
}
