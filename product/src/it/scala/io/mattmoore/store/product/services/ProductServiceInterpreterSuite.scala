package io.mattmoore.store.product.services

import cats.effect.*
import cats.effect.unsafe.implicits.global
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.munit.TestContainersForEach
import doobie.util.transactor.Transactor
import io.mattmoore.store.product.algebras.*
import io.mattmoore.store.product.domain.*
import io.mattmoore.store.product.repositories.*
import natchez.*
import natchez.Trace.Implicits.*
import org.flywaydb.core.Flyway

import java.util.UUID

class ProductServiceInterpreterSuite extends munit.FunSuite with TestContainersForEach {
  override type Containers = PostgreSQLContainer

  override def startContainers(): PostgreSQLContainer = {
    val psql = PostgreSQLContainer.Def("postgres:14").start()
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

  test("getProduct returns a product for the ID") {
    withContainers { case psql =>
      val repository: Repository[F, Product] = new ProductRepositoryInterpreter(
        Transactor.fromDriverManager[F](
          psql.container.getDriverClassName,
          s"${psql.container.getJdbcUrl}/${psql.container.getDatabaseName}",
          psql.container.getUsername,
          psql.container.getPassword
        )
      )
      val service: ProductService[F] = new ProductServiceInterpreter[F](repository)

      val productToAdd = Product(
        name = "Playstation 5",
        description = "Playstation 5",
        price = BigDecimal(499.99)
      )

      val dbProductId = service.addProduct(productToAdd).unsafeRunSync()

      val expected = Product(
        id = Some(dbProductId),
        name = "Playstation 5",
        description = "Playstation 5",
        price = BigDecimal(499.99)
      )
      val actual = service.getProduct(dbProductId).unsafeRunSync()
      assertEquals(actual, expected)
    }
  }

  test("addProduct adds a product and returns the updated product record") {
    withContainers { case psql =>
      val repository: Repository[F, Product] = new ProductRepositoryInterpreter(
        Transactor.fromDriverManager[F](
          psql.container.getDriverClassName,
          s"${psql.container.getJdbcUrl}/${psql.container.getDatabaseName}",
          psql.container.getUsername,
          psql.container.getPassword
        )
      )
      val service: ProductService[F] = new ProductServiceInterpreter[F](repository)
      val productToAdd = Product(
        id = Some(UUID.fromString("32fe8628-4182-4900-9e52-b3c5304f97da")),
        name = "Playstation 5",
        description = "Playstation 5",
        price = BigDecimal(499.99)
      )
      val actual = service.addProduct(productToAdd).unsafeRunSync()
      assert(!actual.toString.isEmpty)
    }
  }

  test("updateProduct updates an existing product and returns the updated product record") {
    withContainers { case psql =>
      val repository: Repository[F, Product] = new ProductRepositoryInterpreter(
        Transactor.fromDriverManager[F](
          psql.container.getDriverClassName,
          s"${psql.container.getJdbcUrl}/${psql.container.getDatabaseName}",
          psql.container.getUsername,
          psql.container.getPassword
        )
      )
      val service: ProductService[F] = new ProductServiceInterpreter[F](repository)

      val initialProduct = Product(
        name = "Playstation 5",
        description = "Playstation 5",
        price = BigDecimal(499.99)
      )
      val productUpdate = Product(
        name = "Playstation 5",
        description = "Playstation 5",
        price = BigDecimal(1000)
      )

      val expected = service.addProduct(initialProduct).unsafeRunSync()
      val actual = service.updateProduct(productUpdate.copy(id = Some(expected))).unsafeRunSync()
      assertEquals(actual, expected)
    }
  }
}
