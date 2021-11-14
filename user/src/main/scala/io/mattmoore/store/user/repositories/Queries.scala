package io.mattmoore.store.user.repositories

import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import io.mattmoore.store.user.domain._

import java.util.UUID

object Queries {
  def selectUser(id: UUID): Query0[User] =
    sql"""SELECT
         |  id,
         |  first_name,
         |  last_name,
         |  email,
         |  address
         |FROM
         |  users
         |WHERE
         |  id = $id
         |""".stripMargin
      .query[User]

  def insertUser(user: User): Update0 = {
    import user._
    sql"""INSERT INTO users (
         |  first_name,
         |  last_name,
         |  email,
         |  address
         |) VALUES (
         |  $firstName,
         |  $lastName,
         |  $email,
         |  $address
         |)
         |""".stripMargin.update
  }

  def updateUser(user: User): Update0 = {
    import user._
    sql"""UPDATE users SET
         |  first_name = $firstName,
         |  last_name = $lastName,
         |  email = $email,
         |  address = $address
         |""".stripMargin.update
  }
}
