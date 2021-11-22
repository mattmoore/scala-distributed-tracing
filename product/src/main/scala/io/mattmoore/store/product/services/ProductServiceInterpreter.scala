package io.mattmoore.store.product.services

import cats.effect.*
import io.mattmoore.store.algebras.*
import io.mattmoore.store.product.algebras.*
import io.mattmoore.store.product.domain.*
import natchez.Trace

import java.util.UUID

class ProductServiceInterpreter[F[_]: Async: Trace](repository: RepositoryAlgebra[F, Product]) extends ProductService[F] {
  override def get(id: UUID): F[Product] =
    Trace[F].span(s"Get product with ID $id") {
      repository.query(id)
    }

  override def add(product: Product): F[UUID] =
    Trace[F].span(s"Add product with info $product") {
      repository.insert(product)
    }

  override def update(product: Product): F[UUID] =
    Trace[F].span(s"Update product with info $product") {
      repository.update(product)
    }
}
