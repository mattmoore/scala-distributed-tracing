package io.mattmoore.store.user.database

import cats.effect._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import io.mattmoore.store.user.algebras._
import io.mattmoore.store.user.domain.User

import java.util.UUID

class Database[F[_]: Async](xa: Transactor[F]) extends DatabaseAlgebra[F] {
  override def getUser(id: UUID): F[User] =
    Queries
      .selectUser(id)
      .unique
      .transact(xa)

  override def addUser(user: User): F[UUID] =
    Queries
      .insertUser(user)
      .withUniqueGeneratedKeys[UUID]("id")
      .transact(xa)

  override def updateUser(user: User): F[UUID] =
    Queries
      .updateUser(user)
      .withUniqueGeneratedKeys[UUID]("id")
      .transact(xa)
}
