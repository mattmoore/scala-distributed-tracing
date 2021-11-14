package io.mattmoore.store.user.algebras

import io.mattmoore.store.user.domain._

import java.util.UUID

trait Repository[F[_], A] {
  def query(id: UUID): F[User]
  def insert(item: A): F[UUID]
  def update(item: A): F[UUID]
}
