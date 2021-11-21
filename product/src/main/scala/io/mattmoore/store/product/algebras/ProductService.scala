package io.mattmoore.store.product.algebras

import io.mattmoore.store.product.domain._

import java.util.UUID

trait ProductService[F[_]] {
  def get(id: UUID): F[Product]
  def add(product: Product): F[UUID]
  def update(product: Product): F[UUID]
}
