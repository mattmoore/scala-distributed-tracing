package io.mattmoore.store.user.algebras

import io.mattmoore.store.user.domain._

import java.util.UUID

trait Repository[F[_]] {
  def getUser(id: UUID): F[User]
  def addUser(user: User): F[UUID]
  def updateUser(user: User): F[UUID]
}
