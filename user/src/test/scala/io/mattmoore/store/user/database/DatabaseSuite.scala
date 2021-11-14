package io.mattmoore.store.user.database

import cats.effect._
import cats.effect.unsafe.implicits.global
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.munit.TestContainersForEach
import doobie.util.transactor.Transactor
import io.mattmoore.store.user.algebras._
import io.mattmoore.store.user.domain._
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.Configuration

import java.util.UUID

class UserRepositorySuite extends munit.FunSuite with TestContainersForEach {
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
      val repo: Repository[F, User] = new UserRepository(
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

      val dbUserId = repo.insert(userToAdd).unsafeRunSync()

      val expected = User(
        id = Some(dbUserId),
        firstName = "Matt",
        lastName = "Moore",
        email = "matt@mattmoore.io",
        address = "123 Anywhere Street, Chicago, IL"
      )
      val actual = repo.query(dbUserId).unsafeRunSync()
      assertEquals(actual, expected)
    }
  }

  test("addUser adds a user and returns the new user's ID") {
    withContainers { case psql =>
      val repo: Repository[F, User] = new UserRepository(
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
      val actual = repo.insert(userToAdd).unsafeRunSync()
      assert(!actual.toString.isEmpty)
    }
  }

  test("updateUser updates an existing user and returns the updated user record") {
    withContainers { case psql =>
      val repo: Repository[F, User] = new UserRepository(
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

      val expected = repo.insert(initialUser).unsafeRunSync()
      val actual = repo.update(userUpdate).unsafeRunSync()
      assertEquals(actual, expected)
    }
  }
}
