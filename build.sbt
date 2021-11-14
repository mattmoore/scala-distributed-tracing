val scalaV2 = "2.13.6"
val scalaV3 = "3.1.0"

val CatsCoreV = "2.6.1"
val CatsEffectV = "3.2.9"
val Http4sV = "0.23.6"
val CirceV = "0.14.1"
val DoobieV = "1.0.0-RC1"
val FlywayV = "8.0.2"
val Fs2V = "3.2.2"
val Fs2KafkaV = "2.2.0"
val MunitV = "0.7.29"
val NatchezV = "0.1.5"
val LogbackV = "1.2.6"
val MunitCatsEffectV = "1.0.6"
val TestContainersV = "0.39.11"

lazy val root = project
  .in(file("."))
  .settings(
    name := "store"
  )

lazy val CommonSettings = Seq(
  scalaVersion := scalaV3,
  crossScalaVersions := Seq(scalaV3, scalaV2),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % CatsCoreV,
    "org.typelevel" %% "cats-effect" % CatsEffectV,
    "co.fs2" %% "fs2-core" % Fs2V,
    "co.fs2" %% "fs2-io" % Fs2V,
    "co.fs2" %% "fs2-reactive-streams" % Fs2V,
    "com.github.fd4s" %% "fs2-kafka" % Fs2KafkaV,
    "org.tpolecat" %% "natchez-core" % NatchezV,
    "org.tpolecat" %% "doobie-core" % DoobieV,
    "org.tpolecat" %% "doobie-h2" % DoobieV,
    "org.tpolecat" %% "doobie-hikari" % DoobieV,
    "org.tpolecat" %% "doobie-postgres" % DoobieV,
    "org.flywaydb" % "flyway-core" % FlywayV,
    "ch.qos.logback" % "logback-classic" % LogbackV,
    "org.scalameta" %% "munit" % MunitV % Test,
    "org.typelevel" %% "munit-cats-effect-3" % MunitCatsEffectV % Test,
    "com.dimafeng" %% "testcontainers-scala-munit" % TestContainersV % "test,it",
    "com.dimafeng" %% "testcontainers-scala-postgresql" % TestContainersV % "test,it"
  )
)

lazy val user = project
  .settings(CommonSettings)
  .settings(
    name := "user",
    version := "0.1.0",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sV,
      "org.http4s" %% "http4s-blaze-client" % Http4sV,
      "org.http4s" %% "http4s-circe" % Http4sV,
      "org.http4s" %% "http4s-dsl" % Http4sV,
      "io.circe" %% "circe-generic" % CirceV
    )
  )
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)

lazy val product = project
  .settings(CommonSettings)
  .settings(
    name := "product",
    version := "0.1.0",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sV,
      "org.http4s" %% "http4s-blaze-client" % Http4sV,
      "org.http4s" %% "http4s-circe" % Http4sV,
      "org.http4s" %% "http4s-dsl" % Http4sV,
      "io.circe" %% "circe-generic" % CirceV
    )
  )
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
