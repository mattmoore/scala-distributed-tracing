package io.mattmoore.store.user.services

import cats.effect.*
import cats.effect.unsafe.implicits.global
import com.dimafeng.testcontainers.munit.TestContainersForEach
import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie.util.transactor.Transactor
import io.mattmoore.store.user.algebras.*
import io.mattmoore.store.user.repositories.*
import io.mattmoore.store.user.domain.*
import natchez._
import natchez.Trace.Implicits._

import java.util.UUID
import org.flywaydb.core.Flyway

class UserServiceInterpreterSuite extends munit.FunSuite with TestContainersForEach {
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

  test("get returns a user for the ID") {
    withContainers { case psql =>
      val userRepository: Repository[F, User] = new UserRepositoryInterpreter(
        Transactor.fromDriverManager[F](
          psql.container.getDriverClassName,
          s"${psql.container.getJdbcUrl}/${psql.container.getDatabaseName}",
          psql.container.getUsername,
          psql.container.getPassword
        )
      )
      val userService: UserService[F] = new UserServiceInterpreter[F](userRepository)

      val userToAdd = User(
        firstName = "Matt",
        lastName = "Moore",
        email = "matt@mattmoore.io",
        address = "123 Anywhere Street, Chicago, IL"
      )

      val dbUserId = userService.add(userToAdd).unsafeRunSync()

      val expected = User(
        id = Some(dbUserId),
        firstName = "Matt",
        lastName = "Moore",
        email = "matt@mattmoore.io",
        address = "123 Anywhere Street, Chicago, IL"
      )
      val actual = userService.get(dbUserId).unsafeRunSync()
      assertEquals(actual, expected)
    }
  }

  test("add adds a user and returns the updated user record") {
    withContainers { case psql =>
      val userRepository: Repository[F, User] = new UserRepositoryInterpreter(
        Transactor.fromDriverManager[F](
          psql.container.getDriverClassName,
          s"${psql.container.getJdbcUrl}/${psql.container.getDatabaseName}",
          psql.container.getUsername,
          psql.container.getPassword
        )
      )
      val userService: UserService[F] = new UserServiceInterpreter[F](userRepository)
      val userToAdd = User(
        id = Some(UUID.fromString("32fe8628-4182-4900-9e52-b3c5304f97da")),
        firstName = "Matt",
        lastName = "Moore",
        email = "matt@mattmoore.io",
        address = "123 Anywhere Street, Chicago, IL"
      )
      val actual = userService.add(userToAdd).unsafeRunSync()
      assert(!actual.toString.isEmpty)
    }
  }

  test("update updates an existing user and returns the updated user record") {
    withContainers { case psql =>
      val userRepository: Repository[F, User] = new UserRepositoryInterpreter(
        Transactor.fromDriverManager[F](
          psql.container.getDriverClassName,
          s"${psql.container.getJdbcUrl}/${psql.container.getDatabaseName}",
          psql.container.getUsername,
          psql.container.getPassword
        )
      )
      val userService: UserService[F] = new UserServiceInterpreter[F](userRepository)

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

      val expected = userService.add(initialUser).unsafeRunSync()
      val actual = userService.update(userUpdate.copy(id = Some(expected))).unsafeRunSync()
      assertEquals(actual, expected)
    }
  }
}
