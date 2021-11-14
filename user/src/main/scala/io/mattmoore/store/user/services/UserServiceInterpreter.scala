package io.mattmoore.store.user.services

import cats.effect._
import io.mattmoore.store.user.algebras._
import io.mattmoore.store.user.domain._

import java.util.UUID

class UserServiceInterpreter[F[_]: Async](repository: Repository[F, User]) extends UserService[F] {
  def getUser(id: UUID): F[User] =
    repository.query(id)

  def addUser(user: User): F[UUID] =
    repository.insert(user)

  def updateUser(user: User): F[UUID] =
    repository.update(user)
}
