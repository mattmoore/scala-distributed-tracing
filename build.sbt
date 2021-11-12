val scala2Version = "2.13.6"
val scala3Version = "3.1.0"

val Http4sVersion              = "0.23.6"
val CirceVersion               = "0.14.1"
val DoobieVersion              = "1.0.0-RC1"
val FlywayVersion              = "8.0.2"
val MunitVersion               = "0.7.29"
val LogbackVersion             = "1.2.6"
val MunitCatsEffectVersion     = "1.0.6"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala3-cross",
    version := "0.1.0",

    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-blaze-server"           % Http4sVersion,
      "org.http4s"            %% "http4s-blaze-client"           % Http4sVersion,
      "org.http4s"            %% "http4s-circe"                  % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"                    % Http4sVersion,
      "io.circe"              %% "circe-generic"                 % CirceVersion,
      "org.tpolecat"          %% "doobie-core"                   % DoobieVersion,
      "org.tpolecat"          %% "doobie-h2"                     % DoobieVersion,
      "org.tpolecat"          %% "doobie-hikari"                 % DoobieVersion,
      "org.tpolecat"          %% "doobie-postgres"               % DoobieVersion,
      "org.flywaydb"           % "flyway-core"                   % FlywayVersion,
      "ch.qos.logback"         % "logback-classic"               % LogbackVersion,
      "org.scalameta"         %% "munit"                         % MunitVersion           % Test,
      "org.typelevel"         %% "munit-cats-effect-3"           % MunitCatsEffectVersion % Test,
    ),

    // To make the default compiler and REPL use Dotty
    scalaVersion := scala3Version,

    // To cross compile with Scala 3 and Scala 2
    crossScalaVersions := Seq(scala3Version, scala2Version)
  )

lazy val product = project
  .settings(
    name := "product"
  )
