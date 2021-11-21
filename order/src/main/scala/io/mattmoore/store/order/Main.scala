package io.mattmoore.store.order

import cats.effect._
import doobie._
import fs2.kafka._
import natchez._
import org.flywaydb.core._
import io.mattmoore.store.order.algebras._
import io.mattmoore.store.order.domain._
import io.mattmoore.store.order.repositories._
import io.mattmoore.store.order.services._

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
      ep.root("Starting the app").use { span =>
        Trace.ioTrace(span).flatMap { implicit trace =>
          implicit val natchez = span
          val xa: Transactor[F] = Transactor.fromDriverManager[F](
            "org.postgresql.Driver",
            "jdbc:postgresql:users",
            "postgres",
            "password"
          )
          Flyway
            .configure()
            .mixed(true)
            .baselineOnMigrate(true)
            .dataSource("jdbc:postgresql:users", "postgres", "password")
            .load()
            .migrate()
          val orderRepository: Repository[F, Order] = new OrderRepositoryInterpreter(xa)
          val orderService: OrderService[F] = new OrderServiceInterpreter[F](orderRepository)

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
  }
}
