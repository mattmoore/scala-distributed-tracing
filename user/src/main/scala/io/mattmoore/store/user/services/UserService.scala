package io.mattmoore.store.user.services

import cats.effect._
import io.mattmoore.store.user.algebras._
import io.mattmoore.store.user.domain._

import java.util.UUID

class UserService[F[_]: Async](db: Repository[F, User]) extends UserServiceAlgebra[F] {
  def getUser(id: UUID): F[User] =
    db.query(id)

  def addUser(user: User): F[UUID] =
    db.insert(user)

  def updateUser(user: User): F[UUID] =
    db.update(user)
}
