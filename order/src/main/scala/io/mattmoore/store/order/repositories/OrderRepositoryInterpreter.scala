package io.mattmoore.store.order.repositories

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import io.mattmoore.store.algebras.*
import io.mattmoore.store.order.domain.*
import natchez.Trace

import java.util.UUID

class OrderRepositoryInterpreter[F[_]: Async: Trace](xa: Transactor[F]) extends RepositoryAlgebra[F, Order] {
  override def query(id: UUID): F[Order] =
    Trace[F].span(s"Fetching order with ID $id from database.") {
      Queries
        .selectOrder(id)
        .unique
        .transact(xa)
    }

  override def insert(order: Order): F[UUID] =
    Trace[F].span(s"Insert new order $order") {
      Queries
        .insertOrder(order)
        .withUniqueGeneratedKeys[UUID]("id")
        .transact(xa)
    }

  override def update(order: Order): F[UUID] =
    Trace[F].span(s"Update order $order") {
      Queries
        .updateOrder(order)
        .withUniqueGeneratedKeys[UUID]("id")
        .transact(xa)
    }
}
