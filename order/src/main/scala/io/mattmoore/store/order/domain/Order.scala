package io.mattmoore.store.order.domain

import java.util.UUID

case class Order(
  id: Option[UUID] = None,
  userId: UUID,
  productId: UUID
)
