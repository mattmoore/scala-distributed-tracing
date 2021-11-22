package io.mattmoore.store.product.algebras

import io.mattmoore.store.algebras.*
import io.mattmoore.store.product.domain.*

import java.util.UUID

trait ProductServiceAlgebra[F[_]] extends ServiceAlgebra[F, Product] {
  def get(id: UUID): F[Product]
  def add(product: Product): F[UUID]
  def update(product: Product): F[UUID]
}
