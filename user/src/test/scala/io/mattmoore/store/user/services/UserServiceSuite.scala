package io.mattmoore.store.user.services

import cats.effect._
import cats.effect.unsafe.implicits.global
import com.dimafeng.testcontainers.munit.TestContainersForAll
import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie.util.transactor.Transactor
import io.mattmoore.store.user.algebras._
import io.mattmoore.store.user.database._
import io.mattmoore.store.user.domain._

import java.util.UUID

class UserServiceSuite extends munit.FunSuite with TestContainersForAll {
  override type Containers = PostgreSQLContainer

  override def startContainers(): PostgreSQLContainer = {
    PostgreSQLContainer.Def().start()
  }

  type F[A] = IO[A]

  val xa: Transactor[F] = Transactor.fromDriverManager[F](
    "org.postgresql.Driver",
    "jdbc:postgresql:user",
    "postgres",
    ""
  )
  val userRepository: Repository[F, User] = new UserRepository(xa)
  val userService: UserService[F] = new UserServiceInterpreter[F](userRepository)

  test("getUser returns a user for the ID") {
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

  test("addUser adds a user and returns the updated user record") {
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

  test("updateUser updates an existing user and returns the updated user record") {
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
