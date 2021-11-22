package io.mattmoore.store.user.algebras

import io.mattmoore.store.algebras.*
import io.mattmoore.store.user.domain.*

import java.util.UUID

trait UserServiceAlgebra[F[_]] extends ServiceAlgebra[F, User] {
  def get(id: UUID): F[User]
  def add(user: User): F[UUID]
  def update(user: User): F[UUID]
}
