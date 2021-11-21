package io.mattmoore.store.product.repositories

import cats.effect.*
import doobie.*
import io.mattmoore.store.product.algebras.*
import io.mattmoore.store.product.domain.*
import natchez.*

import java.util.UUID

class ProductRepository[F[_]: Async, Trace](transactor: Transactor[F]) extends Repository[F, Product] {
  def query(id: UUID): F[Product] = ???
  def insert(item: Product): F[UUID] = ???
  def update(item: Product): F[UUID] = ???
}
