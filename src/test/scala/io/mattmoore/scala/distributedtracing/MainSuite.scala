package io.mattmoore.scala.distributedtracing

import io.mattmoore.scala.distributedtracing.Main

class MainSuite extends munit.FunSuite {
  test("Scala 3 compiles") {
    assertEquals("I was compiled by Scala 3. :)", Main.msg)
  }
}
