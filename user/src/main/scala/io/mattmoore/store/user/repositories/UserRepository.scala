package io.mattmoore.store.user.repositories

import cats.effect._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import io.mattmoore.store.user.algebras._
import io.mattmoore.store.user.domain.User

import java.util.UUID

class UserRepository[F[_]: Async](xa: Transactor[F]) extends Repository[F, User] {
  override def query(id: UUID): F[User] =
    Queries
      .selectUser(id)
      .unique
      .transact(xa)

  override def insert(user: User): F[UUID] =
    Queries
      .insertUser(user)
      .withUniqueGeneratedKeys[UUID]("id")
      .transact(xa)

  override def update(user: User): F[UUID] =
    Queries
      .updateUser(user)
      .withUniqueGeneratedKeys[UUID]("id")
      .transact(xa)
}
