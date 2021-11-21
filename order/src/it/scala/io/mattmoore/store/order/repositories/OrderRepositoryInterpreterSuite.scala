package io.mattmoore.store.order.repositories

import cats.effect.*
import cats.effect.unsafe.implicits.global
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.munit.TestContainersForEach
import doobie.util.transactor.Transactor
import io.mattmoore.store.order.algebras.*
import io.mattmoore.store.order.domain.*
import io.mattmoore.store.order.repositories.*
import natchez.*
import natchez.Trace.Implicits.*
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.Configuration

import java.util.UUID
import scala.jdk.CollectionConverters.*

class OrderRepositoryInterpreterSuite extends munit.FunSuite with TestContainersForEach {
  override type Containers = PostgreSQLContainer

  override def startContainers(): PostgreSQLContainer = {
    val psql = PostgreSQLContainer("postgres:14").configure { c =>
      c.withExposedPorts(5432)
      c.setPortBindings(List("5432:5432").asJava)
    }
    psql.start()
    val flyway = Flyway
      .configure()
      .mixed(true)
      .baselineOnMigrate(true)
      .dataSource(psql.container.getJdbcUrl, psql.container.getUsername, psql.container.getPassword)
      .load()
      .migrate()
    psql
  }

  type F[A] = IO[A]

  def entryPoint[F[_]: Sync]: Resource[F, EntryPoint[F]] = {
    import io.jaegertracing.Configuration.{ReporterConfiguration, SamplerConfiguration}
    import natchez.jaeger.Jaeger
    Jaeger.entryPoint[F]("ProductService") { c =>
      Sync[F].delay {
        c.withSampler(new SamplerConfiguration().withType("const").withParam(1))
          .withReporter(ReporterConfiguration.fromEnv)
          .getTracer
      }
    }
  }

  test("getOrder returns an order for the ID") {
    withContainers { case psql =>
      val orderRepository: Repository[F, Order] = new OrderRepositoryInterpreter(
        Transactor.fromDriverManager[F](
          psql.container.getDriverClassName,
          s"${psql.container.getJdbcUrl}/${psql.container.getDatabaseName}",
          psql.container.getUsername,
          psql.container.getPassword
        )
      )

      val orderToAdd = Order(
        userId = UUID.fromString("c9ef06bb-940e-4aef-b0ac-0d603d1b50d8"),
        productId = UUID.fromString("ca81d811-befe-4e44-9e59-e5260598a22d")
      )

      val dbOrderId = orderRepository.insert(orderToAdd).unsafeRunSync()

      val expected = Order(
        id = Some(dbOrderId),
        userId = UUID.fromString("c9ef06bb-940e-4aef-b0ac-0d603d1b50d8"),
        productId = UUID.fromString("ca81d811-befe-4e44-9e59-e5260598a22d")
      )
      val actual = orderRepository.query(dbOrderId).unsafeRunSync()
      assertEquals(actual, expected)
    }
  }

  test("addOrder adds an order and returns the new ID") {
    withContainers { case psql =>
      val orderRepository: Repository[F, Order] = new OrderRepositoryInterpreter(
        Transactor.fromDriverManager[F](
          psql.container.getDriverClassName,
          s"${psql.container.getJdbcUrl}/${psql.container.getDatabaseName}",
          psql.container.getUsername,
          psql.container.getPassword
        )
      )
      val orderToAdd = Order(
        id = Some(UUID.fromString("32fe8628-4182-4900-9e52-b3c5304f97da")),
        userId = UUID.fromString("c9ef06bb-940e-4aef-b0ac-0d603d1b50d8"),
        productId = UUID.fromString("ca81d811-befe-4e44-9e59-e5260598a22d")
      )
      val actual = orderRepository.insert(orderToAdd).unsafeRunSync()
      assert(!actual.toString.isEmpty)
    }
  }

  test("updateOrder updates an existing order and returns the updated record") {
    withContainers { case psql =>
      val orderRepository: Repository[F, Order] = new OrderRepositoryInterpreter(
        Transactor.fromDriverManager[F](
          psql.container.getDriverClassName,
          s"${psql.container.getJdbcUrl}/${psql.container.getDatabaseName}",
          psql.container.getUsername,
          psql.container.getPassword
        )
      )
      val initialOrder = Order(
        userId = UUID.fromString("c9ef06bb-940e-4aef-b0ac-0d603d1b50d8"),
        productId = UUID.fromString("ca81d811-befe-4e44-9e59-e5260598a22d")
      )
      val orderUpdate = Order(
        userId = UUID.fromString("c9ef06bb-940e-4aef-b0ac-0d603d1b50d8"),
        productId = UUID.fromString("ca81d811-befe-4e44-9e59-e5260598a22d")
      )

      val expected = orderRepository.insert(initialOrder).unsafeRunSync()
      val actual = orderRepository.update(orderUpdate.copy(id = Some(expected))).unsafeRunSync()
      assertEquals(actual, expected)
    }
  }
}
