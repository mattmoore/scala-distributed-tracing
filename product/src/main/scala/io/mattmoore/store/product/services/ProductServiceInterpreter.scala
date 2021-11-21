package io.mattmoore.store.product.services

import cats.effect.*
import io.mattmoore.store.product.algebras.*
import io.mattmoore.store.product.domain.*
import natchez.Trace

import java.util.UUID

class ProductServiceInterpreter[F[_]: Async: Trace](repository: Repository[F, Product]) extends ProductService[F] {
  override def getProduct(id: UUID): F[Product] =
    Trace[F].span(s"Get product with ID $id") {
      repository.query(id)
    }

  override def addProduct(product: Product): F[UUID] =
    Trace[F].span(s"Add product with info $product") {
      repository.insert(product)
    }

  override def updateProduct(product: Product): F[UUID] =
    Trace[F].span(s"Update product with info $product") {
      repository.update(product)
    }
}
