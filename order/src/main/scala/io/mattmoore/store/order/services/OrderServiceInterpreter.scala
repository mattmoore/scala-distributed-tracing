package io.mattmoore.store.order.services

import cats.effect.*
import io.mattmoore.store.order.algebras.*
import io.mattmoore.store.order.domain.*
import natchez.Trace

import java.util.UUID

class OrderServiceInterpreter[F[_]: Async: Trace](repository: Repository[F, Order]) extends OrderService[F] {
  override def getOrder(id: UUID): F[Order] =
    Trace[F].span(s"Get order with ID $id") {
      repository.query(id)
    }

  override def addOrder(order: Order): F[UUID] =
    Trace[F].span(s"Add order with info $order") {
      repository.insert(order)
    }

  override def updateOrder(order: Order): F[UUID] =
    Trace[F].span(s"Update order with info $order") {
      repository.update(order)
    }
}
