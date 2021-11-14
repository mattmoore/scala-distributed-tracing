package io.mattmoore.store.user.services

import cats.effect._
import cats.effect.unsafe.implicits.global
import com.dimafeng.testcontainers.munit.TestContainersForEach
import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie.util.transactor.Transactor
import io.mattmoore.store.user.algebras._
import io.mattmoore.store.user.database._
import io.mattmoore.store.user.domain._

import java.util.UUID
import org.flywaydb.core.Flyway

class UserServiceSuite extends munit.FunSuite with TestContainersForEach {
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

  test("getUser returns a user for the ID") {

    withContainers { case psql =>
      val userRepository: Repository[F, User] = new UserRepository(
        Transactor.fromDriverManager[F](
          psql.container.getDriverClassName,
          s"${psql.container.getJdbcUrl}/${psql.container.getDatabaseName}",
          psql.container.getUsername,
          psql.container.getPassword
        )
      )
      val userService: UserService[F] = new UserServiceInterpreter[F](userRepository)
      val expected = User(
        id = Some(UUID.fromString("32fe8628-4182-4900-9e52-b3c5304f97da")),
        firstName = "Matt",
        lastName = "Moore",
        email = "matt@mattmoore.io",
        address = "123 Anywhere Street, Chicago, IL"
      )
      val actual = userService.getUser(expected.id.get).unsafeRunSync()
      assertEquals(actual, expected)
    }
  }

  test("addUser adds a user and returns the updated user record") {
    withContainers { case psql =>
      val userRepository: Repository[F, User] = new UserRepository(
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
      val expected = UUID.fromString("32fe8628-4182-4900-9e52-b3c5304f97da")
      val actual = userService.addUser(userToAdd).unsafeRunSync()
      assertEquals(actual, expected)
    }
  }

  test("updateUser updates an existing user and returns the updated user record") {
    withContainers { case psql =>
      val userRepository: Repository[F, User] = new UserRepository(
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
      val userToUpdate = User(
        id = Some(UUID.fromString("32fe8628-4182-4900-9e52-b3c5304f97da")),
        firstName = "Matthew",
        lastName = "Moore",
        email = "matt@mattmoore.io",
        address = "123 Anywhere Street, Chicago, IL"
      )

      userService.addUser(initialUser).unsafeRunSync()

      val expected = UUID.fromString("32fe8628-4182-4900-9e52-b3c5304f97da")
      val actual = userService.updateUser(userToUpdate).unsafeRunSync()
      assertEquals(actual, expected)
    }
  }
}
