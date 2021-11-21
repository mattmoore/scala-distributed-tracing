package io.mattmoore.store.product.algebras

import io.mattmoore.store.product.domain._

import java.util.UUID

trait Repository[F[_], A] {
  def query(id: UUID): F[Product]
  def insert(item: A): F[UUID]
  def update(item: A): F[UUID]
}
