package io.mattmoore.store.user.domain

import java.util.UUID

case class User(
  id: Option[UUID] = None,
  firstName: String,
  lastName: String,
  email: String,
  address: String
)
