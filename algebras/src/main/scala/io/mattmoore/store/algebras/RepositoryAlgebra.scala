package io.mattmoore.store.algebras

import java.util.UUID

trait RepositoryAlgebra[F[_], A] {
  def query(id: UUID): F[A]
  def insert(item: A): F[UUID]
  def update(item: A): F[UUID]
}
