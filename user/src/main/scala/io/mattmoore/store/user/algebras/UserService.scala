package io.mattmoore.store.user.algebras

import io.mattmoore.store.user.domain._

import java.util.UUID

trait UserService[F[_]] {
  def get(id: UUID): F[User]
  def add(user: User): F[UUID]
  def update(user: User): F[UUID]
}
