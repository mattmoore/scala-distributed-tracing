package io.mattmoore.store.user.services

import cats.effect.*
import io.mattmoore.store.algebras.*
import io.mattmoore.store.user.algebras.*
import io.mattmoore.store.user.domain.*
import natchez.Trace

import java.util.UUID

class UserServiceInterpreter[F[_]: Async: Trace](repository: RepositoryAlgebra[F, User]) extends UserServiceAlgebra[F] {
  override def get(id: UUID): F[User] =
    Trace[F].span(s"Get user with ID $id") {
      repository.query(id)
    }

  override def add(user: User): F[UUID] =
    Trace[F].span(s"Add user with info $user") {
      repository.insert(user)
    }

  override def update(user: User): F[UUID] =
    Trace[F].span(s"Update user with info $user") {
      repository.update(user)
    }
}
