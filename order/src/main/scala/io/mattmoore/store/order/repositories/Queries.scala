package io.mattmoore.store.order.repositories

import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import io.mattmoore.store.order.domain.*

import java.util.UUID

object Queries {
  def selectOrder(id: UUID): Query0[Order] =
    sql"""SELECT
         |  id,
         |  userId,
         |  productId
         |FROM
         |  orders
         |WHERE
         |  id = $id
         |""".stripMargin
      .query[Order]

  def insertOrder(order: Order): Update0 = {
    import order.*
    sql"""INSERT INTO orders (
         |  userId,
         |  productId
         |) VALUES (
         |  $userId,
         |  $productId
         |)
         |""".stripMargin.update
  }

  def updateOrder(order: Order): Update0 = {
    import order.*
    sql"""UPDATE orders SET
         |  userId = $userId,
         |  productId = $productId
         |WHERE
         |  id = $id
         |""".stripMargin.update
  }
}
