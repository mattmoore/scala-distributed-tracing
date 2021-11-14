package io.mattmoore.store.user.database

import cats.effect._
import cats.effect.unsafe.implicits.global
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.munit.TestContainersForAll
import doobie.util.transactor.Transactor
import io.mattmoore.store.user.algebras._
import io.mattmoore.store.user.domain._

import java.util.UUID

class DatabaseSuite extends munit.FunSuite with TestContainersForAll {
  override type Containers = PostgreSQLContainer

  override def startContainers(): PostgreSQLContainer = {
    PostgreSQLContainer.Def().start()
  }

  type F[A] = IO[A]

  test("getUser returns a user for the ID") {
    withContainers { case psql =>
      val db: DatabaseAlgebra[F] = new Database(
        Transactor.fromDriverManager[F](
          psql.container.getDriverClassName,
          s"${psql.container.getJdbcUrl}/${psql.container.getDatabaseName}",
          psql.container.getUsername,
          psql.container.getPassword
        )
      )

      val expected = User(
        id = Some(UUID.fromString("32fe8628-4182-4900-9e52-b3c5304f97da")),
        firstName = "Matt",
        lastName = "Moore",
        email = "matt@mattmoore.io",
        address = "123 Anywhere Street, Chicago, IL"
      )
      val actual = db.getUser(expected.id.get).unsafeRunSync()
      assertEquals(actual, expected)
    }
  }

  test("addUser adds a user and returns the updated user record") {
    withContainers { case psql =>
      val db: DatabaseAlgebra[F] = new Database(
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
      val expected = UUID.fromString("32fe8628-4182-4900-9e52-b3c5304f97da")
      val actual = db.addUser(userToAdd).unsafeRunSync()
      assertEquals(actual, expected)
    }
  }

  test("updateUser updates an existing user and returns the updated user record") {
    withContainers { case psql =>
      val db: DatabaseAlgebra[F] = new Database(
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
      val userToUpdate = User(
        id = Some(UUID.fromString("32fe8628-4182-4900-9e52-b3c5304f97da")),
        firstName = "Matthew",
        lastName = "Moore",
        email = "matt@mattmoore.io",
        address = "123 Anywhere Street, Chicago, IL"
      )

      db.addUser(initialUser).unsafeRunSync()

      val expected = UUID.fromString("32fe8628-4182-4900-9e52-b3c5304f97da")
      val actual = db.updateUser(userToUpdate).unsafeRunSync()
      assertEquals(actual, expected)
    }
  }
}
