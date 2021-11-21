package io.mattmoore.store.product.algebras

import java.util.UUID

trait Repository[F[_], A] {
  def query(id: UUID): F[A]
  def insert(item: A): F[UUID]
  def update(item: A): F[UUID]
}
