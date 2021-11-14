package io.mattmoore.store.user.services

import cats.effect._
import io.mattmoore.store.user.algebras._
import io.mattmoore.store.user.domain._

import java.util.UUID

class UserService[F[_]: Async](db: DatabaseAlgebra[F]) extends UserServiceAlgebra[F] {
  def getUser(id: UUID): F[User] =
    db.getUser(id)

  def addUser(user: User): F[UUID] =
    db.addUser(user)

  def updateUser(user: User): F[UUID] =
    db.updateUser(user)
}
