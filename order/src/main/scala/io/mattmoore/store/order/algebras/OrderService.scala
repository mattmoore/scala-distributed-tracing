package io.mattmoore.store.order.algebras

import io.mattmoore.store.order.domain._

import java.util.UUID

trait OrderService[F[_]] {
  def getOrder(id: UUID): F[Order]
  def addOrder(product: Order): F[UUID]
  def updateOrder(product: Order): F[UUID]
}
