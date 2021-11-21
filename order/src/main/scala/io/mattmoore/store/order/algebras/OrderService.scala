package io.mattmoore.store.order.algebras

import io.mattmoore.store.order.domain._

import java.util.UUID

trait OrderService[F[_]] {
  def get(id: UUID): F[Order]
  def add(product: Order): F[UUID]
  def update(product: Order): F[UUID]
}
