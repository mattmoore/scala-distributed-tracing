package io.mattmoore.store.user.repositories

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import io.mattmoore.store.user.algebras.*
import io.mattmoore.store.user.domain.*
import natchez.Trace

import java.util.UUID

class UserRepository[F[_]: Async: Trace](xa: Transactor[F]) extends Repository[F, User] {
  override def query(id: UUID): F[User] =
    Trace[F].span(s"Fetching user with ID $id from database.") {
      Queries
        .selectUser(id)
        .unique
        .transact(xa)
    }

  override def insert(user: User): F[UUID] =
    Trace[F].span(s"Insert new user $user") {
      Queries
        .insertUser(user)
        .withUniqueGeneratedKeys[UUID]("id")
        .transact(xa)
    }

  override def update(user: User): F[UUID] =
    Trace[F].span(s"Update user $user") {
      Queries
        .updateUser(user)
        .withUniqueGeneratedKeys[UUID]("id")
        .transact(xa)
    }
}
