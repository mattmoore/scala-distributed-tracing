package io.mattmoore.store.product.services

import cats.effect._
import io.mattmoore.store.product.algebras._
import io.mattmoore.store.product.domain._
import natchez.Trace

class ProductServiceInterpreter[F[_]: Async: Trace, A](repository: Repository[F, A]) extends ProductService[F, A] {

}
