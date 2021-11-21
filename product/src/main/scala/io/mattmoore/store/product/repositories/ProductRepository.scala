package io.mattmoore.store.product.repositories

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import io.mattmoore.store.product.algebras.*
import io.mattmoore.store.product.domain.*
import natchez.Trace

import java.util.UUID

class ProductRepository[F[_]: Async: Trace](xa: Transactor[F]) extends Repository[F, Product] {
  override def query(id: UUID): F[Product] =
    Trace[F].span(s"Fetching user with ID $id from database.") {
      Queries
        .selectProduct(id)
        .unique
        .transact(xa)
    }

  override def insert(product: Product): F[UUID] =
    Trace[F].span(s"Insert new product $product") {
      Queries
        .insertProduct(product)
        .withUniqueGeneratedKeys[UUID]("id")
        .transact(xa)
    }

  override def update(product: Product): F[UUID] =
    Trace[F].span(s"Update product $product") {
      Queries
        .updateProduct(product)
        .withUniqueGeneratedKeys[UUID]("id")
        .transact(xa)
    }
}
