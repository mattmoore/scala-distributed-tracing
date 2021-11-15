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
import natchez.Trace.Implicits.noop
import natchez._

object Main extends IOApp {
  type F[A] = IO[A]

  def entryPoint[F[_]: Sync]: Resource[F, EntryPoint[F]] = {
    import natchez.jaeger.Jaeger
    import io.jaegertracing.Configuration.SamplerConfiguration
    import io.jaegertracing.Configuration.ReporterConfiguration
    Jaeger.entryPoint[F]("UserService") { c =>
      Sync[F].delay {
        c.withSampler(new SamplerConfiguration().withType("const").withParam(1))
          .withReporter(ReporterConfiguration.fromEnv)
          .getTracer
      }
    }
  }

  override def run(args: List[String]): F[ExitCode] = {
    entryPoint[F].use { ep =>
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

      IO(ExitCode.Success)
    }
  }
}
