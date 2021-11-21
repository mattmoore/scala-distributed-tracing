package io.mattmoore.store.product.algebras

import io.mattmoore.store.product.domain._

import java.util.UUID

trait ProductService[F[_]] {
  def getProduct(id: UUID): F[Product]
  def addProduct(product: Product): F[UUID]
  def updateProduct(product: Product): F[UUID]
}
