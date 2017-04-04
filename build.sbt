import com.typesafe.sbt.SbtScalariform._
import xerial.sbt.Sonatype._
import org.scoverage.coveralls.Imports.CoverallsKeys._

import scalariform.formatter.preferences._

name := "play-reactivemongo-commons"

organization := "io.soheila"

description := "Common Utilities for working with MongoDB"

homepage := Some(url("http://www.hublove.com/"))

licenses := Seq("Apache2 License" -> url("https://www.apache.org/licenses/LICENSE-2.0"))

version := "0.1.0-alpha1"

scalaVersion := "2.11.8"

aggregate in update := false

libraryDependencies ++= Seq(
  // Persistence
  "org.reactivemongo" %% "play2-reactivemongo" % "0.12.2",
  "com.typesafe.play" %% "play-json" % "2.5.14",
  "ch.qos.logback" % "logback-classic" % "1.2.1",
  "org.slf4j" % "slf4j-simple" % "1.7.23",
  "org.clapper" %% "grizzled-slf4j" % "1.3.0",
  "com.fasterxml.uuid" % "java-uuid-generator" % "3.1.4",
  // DI
  "net.codingwell" %% "scala-guice" % "4.1.0",
  "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.0.0" % Test,
  "net.codingwell" %% "scala-guice" % "4.1.0" % Test,
  "org.specs2" %% "specs2-core" % "3.8.6" % "test",
  "com.typesafe.play" % "play-specs2_2.11" % "2.5.13"
)

lazy val `play-reactive-mongo-commons` = Project(id = "play-reactive-mongo-commons", base = file("."))
  .enablePlugins(JavaAppPackaging)


//*******************************
// Test settings
//*******************************

parallelExecution in Test := false

fork in Test := true

// Needed to avoid https://github.com/travis-ci/travis-ci/issues/3775 in forked tests
// in Travis with `sudo: false`.
// See https://github.com/sbt/sbt/issues/653
// and https://github.com/travis-ci/travis-ci/issues/3775
javaOptions += "-Xmx1G"

//********************************************************
// Scalariform settings
//********************************************************

defaultScalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(FormatXml, false)
  .setPreference(DoubleIndentClassDeclaration, false)
  .setPreference(DanglingCloseParenthesis, Preserve)

updateOptions := updateOptions.in(Global).value.withCachedResolution(true)

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"

scalacOptions ++= Seq(
  "-language:postfixOps", // See the Scala docs for value scala.language.postfixOps for a discussion
  "-Xlint:-missing-interpolator", // ignore error(warning): possible missing interpolator: detected interpolated identifier
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen" // Warn when numerics are widened.
)

scalacOptions in (Compile, doc) ++= Seq(
  "-no-link-warnings" // Suppresses problems with Scaladoc @throws links
)


sonatypeSettings

val pom = <scm>
  <url>git@github.com:esfand-r/play-reactivemongo-commons.git</url>
  <connection>scm:git@github.com:esfand-r/play-reactivemongo-commons.git</connection>
</scm>
  <developers>
    <developer>
      <id>esfand-r</id>
      <name>Esfandiar Amirrahimi</name>
      <url>http://soheila.io</url>
    </developer>
  </developers>;

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

sources in (Compile,doc) := Seq.empty

pomExtra := pom

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

coverallsToken := sys.env.get("COVERALLS_REPO_TOKEN")

addCommandAlias("build",       ";clean;coverage;test;format;coverageReport")
addCommandAlias("deployBuild", ";clean;coverage;test;format;coverageReport;coverageAggregate;coveralls")
