package io.mattmoore.store.user.repositories

import cats.effect.*
import cats.effect.unsafe.implicits.global
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.munit.TestContainersForEach
import doobie.util.transactor.Transactor
import io.mattmoore.store.user.algebras.*
import io.mattmoore.store.user.domain.*
import natchez._
import natchez.Trace.Implicits._
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.Configuration

import java.util.UUID
import scala.jdk.CollectionConverters.*

class UserRepositoryInterpreterSuite extends munit.FunSuite with TestContainersForEach {
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

  test("getUser returns a user for the ID") {
    withContainers { case psql =>
      val userRepository: Repository[F, User] = new UserRepositoryInterpreter(
        Transactor.fromDriverManager[F](
          psql.container.getDriverClassName,
          s"${psql.container.getJdbcUrl}/${psql.container.getDatabaseName}",
          psql.container.getUsername,
          psql.container.getPassword
        )
      )

      val userToAdd = User(
        firstName = "Matt",
        lastName = "Moore",
        email = "matt@mattmoore.io",
        address = "123 Anywhere Street, Chicago, IL"
      )

      val dbUserId = userRepository.insert(userToAdd).unsafeRunSync()

      val expected = User(
        id = Some(dbUserId),
        firstName = "Matt",
        lastName = "Moore",
        email = "matt@mattmoore.io",
        address = "123 Anywhere Street, Chicago, IL"
      )
      val actual = userRepository.query(dbUserId).unsafeRunSync()
      assertEquals(actual, expected)
    }
  }

  test("addUser adds a user and returns the new user's ID") {
    withContainers { case psql =>
      val userRepository: Repository[F, User] = new UserRepositoryInterpreter(
        Transactor.fromDriverManager[F](
          psql.container.getDriverClassName,
          s"${psql.container.getJdbcUrl}/${psql.container.getDatabaseName}",
          psql.container.getUsername,
          psql.container.getPassword
        )
      )
      val userToAdd = User(
        id = Some(UUID.fromString("32fe8628-4182-4900-9e52-b3c5304f97da")),
        firstName = "Matt",
        lastName = "Moore",
        email = "matt@mattmoore.io",
        address = "123 Anywhere Street, Chicago, IL"
      )
      val actual = userRepository.insert(userToAdd).unsafeRunSync()
      assert(!actual.toString.isEmpty)
    }
  }

  test("updateUser updates an existing user and returns the updated user record") {
    withContainers { case psql =>
      val userRepository: Repository[F, User] = new UserRepositoryInterpreter(
        Transactor.fromDriverManager[F](
          psql.container.getDriverClassName,
          s"${psql.container.getJdbcUrl}/${psql.container.getDatabaseName}",
          psql.container.getUsername,
          psql.container.getPassword
        )
      )
      val initialUser = User(
        firstName = "Matt",
        lastName = "Moore",
        email = "matt@mattmoore.io",
        address = "123 Anywhere Street, Chicago, IL"
      )
      val userUpdate = User(
        firstName = "Matthew",
        lastName = "Moore",
        email = "matt@mattmoore.io",
        address = "123 Anywhere Street, Chicago, IL"
      )

      val expected = userRepository.insert(initialUser).unsafeRunSync()
      val actual = userRepository.update(userUpdate).unsafeRunSync()
      assertEquals(actual, expected)
    }
  }
}
