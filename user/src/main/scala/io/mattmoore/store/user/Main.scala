package io.mattmoore.store.user

import cats.effect._
import com.monovore.decline._
import com.monovore.decline.effect._
import doobie._
import fs2.kafka._
import io.mattmoore.store.user.algebras._
import io.mattmoore.store.user.domain._
import io.mattmoore.store.user.repositories._
import io.mattmoore.store.user.services._

object Main extends CommandIOApp(name = "Store Microservices", header = "Store Microservices") {
  type F[A] = IO[A]

  override def main: Opts[F[ExitCode]] = {
    val xa: Transactor[F] = Transactor.fromDriverManager[F](
      "org.postgresql.Driver",
      "jdbc:postgresql:user",
      "postgres",
      "password"
    )
    val userRepository: Repository[F, User] = new UserRepository(xa)
    val userService: UserService[F] = new UserServiceInterpreter[F](userRepository)

    val consumerSettings =
      ConsumerSettings[F, String, String]
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers("localhost:9092")
        .withGroupId("group")

    KafkaConsumer.stream(consumerSettings)

    Opts(IO(ExitCode.Success))
  }
}
