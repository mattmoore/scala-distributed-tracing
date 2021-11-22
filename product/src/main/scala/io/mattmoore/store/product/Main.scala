package io.mattmoore.store.product

import cats.effect.*
import doobie.*
import fs2.kafka.*
import natchez.*
import org.flywaydb.core.*
import io.mattmoore.store.algebras.*
import io.mattmoore.store.product.algebras.*
import io.mattmoore.store.product.domain.*
import io.mattmoore.store.product.repositories.*
import io.mattmoore.store.product.services.*

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
          val productRepository: RepositoryAlgebra[F, Product] = new ProductRepositoryInterpreter(xa)
          val productService: ProductService[F] = new ProductServiceInterpreter[F](productRepository)

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
