package io.mattmoore.store.user.services

import cats.effect._
import io.mattmoore.store.user.algebras._
import io.mattmoore.store.user.domain._
import natchez._

import java.util.UUID

class UserServiceInterpreter[F[_]: Async: natchez.Trace](repository: Repository[F, User])(implicit tracer: natchez.Trace[F]) extends UserService[F] {
  def getUser(id: UUID): F[User] =
    tracer.span(s"Get user with ID $id") {
      repository.query(id)
    }

  def addUser(user: User): F[UUID] =
    tracer.span(s"Add user with info $user") {
      repository.insert(user)
    }

  def updateUser(user: User): F[UUID] =
    tracer.span(s"Update user with info $user") {
      repository.update(user)
    }
}
