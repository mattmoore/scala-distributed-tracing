package io.mattmoore.store.product.repositories

import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import io.mattmoore.store.product.domain.*

import java.util.UUID

object Queries {
  def selectProduct(id: UUID): Query0[Product] =
    sql"""SELECT
         |  id,
         |  name,
         |  description,
         |  price
         |FROM
         |  products
         |WHERE
         |  id = $id
         |""".stripMargin
      .query[Product]

  def insertProduct(product: Product): Update0 = {
    import product.*
    sql"""INSERT INTO products (
         |  name,
         |  description,
         |  price
         |) VALUES (
         |  $name,
         |  $description,
         |  $price
         |)
         |""".stripMargin.update
  }

  def updateProduct(product: Product): Update0 = {
    import product.*
    sql"""UPDATE products SET
         |  name = $name,
         |  description = $description,
         |  price = $price
         |""".stripMargin.update
  }
}
