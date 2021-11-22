package io.mattmoore.store.order.algebras

import io.mattmoore.store.algebras.*
import io.mattmoore.store.order.domain.*

import java.util.UUID

trait OrderServiceAlgebra[F[_]] extends ServiceAlgebra[F, Order] {
  def get(id: UUID): F[Order]
  def add(product: Order): F[UUID]
  def update(product: Order): F[UUID]
}
