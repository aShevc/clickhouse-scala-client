import Build.*

// Scala Formatting
ThisBuild / scalafmtVersion := "1.5.1"
ThisBuild / scalafmtOnCompile := false     // all projects
ThisBuild / scalafmtTestOnCompile := false // all projects

releaseCrossBuild := true

sonatypeProfileName := "com.crobox"

lazy val root = (project in file("."))
  .settings(
    publish := {},
    publishArtifact := false,
    inThisBuild(
      List(
        organization := "com.crobox.clickhouse",
        scalaVersion := "2.13.8",
        crossScalaVersions := List("2.13.8"),
        javacOptions ++= Seq("-g", "-Xlint:unchecked", "-Xlint:deprecation", "-source", "11", "-target", "11"),
        scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:_", "-encoding", "UTF-8"),
        publishTo := {
          val nexus = "https://oss.sonatype.org/"
          if (version.value.trim.endsWith("SNAPSHOT"))
            Some("snapshots" at nexus + "content/repositories/snapshots")
          else
            Some("releases" at nexus + "service/local/staging/deploy/maven2")
        },
        pomExtra := {
          <url>https://github.com/crobox/clickhouse-scala-client</url>
            <licenses>
              <license>
                <name>The GNU Lesser General Public License, Version 3.0</name>
                <url>http://www.gnu.org/licenses/lgpl-3.0.txt</url>
                <distribution>repo</distribution>
              </license>
            </licenses>
            <scm>
              <url>git@github.com:crobox/clickhouse-scala-client.git</url>
              <connection>scm:git@github.com:crobox/clickhouse-scala-client.git</connection>
            </scm>
            <developers>
              <developer>
                <id>crobox</id>
                <name>crobox</name>
                <url>https://github.com/crobox</url>
              </developer>
            </developers>
        }
      )
    ),
    name := "clickhouse"
  )
  .aggregate(client, dsl, testkit)

lazy val client: Project = (project in file("client"))
  .configs(Config.CustomIntegrationTest)
  .settings(Config.testSettings: _*)
  .settings(
    name := "client",
    sbtrelease.ReleasePlugin.autoImport.releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    libraryDependencies ++= Seq(
      "io.spray"                   %% "spray-json"    % "1.3.6",
      "org.apache.pekko"           %% "pekko-actor"   % PekkoVersion,
      "org.apache.pekko"           %% "pekko-stream"  % PekkoVersion,
      "org.apache.pekko"           %% "pekko-http"    % PekkoHttpVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      "joda-time"                  % "joda-time"      % "2.12.5"
    ) ++ Seq("org.apache.pekko"    %% "pekko-testkit" % PekkoVersion % Test) ++ Build.testDependencies.map(_ % Test)
  )

lazy val dsl = (project in file("dsl"))
  .dependsOn(client, client % "test->test", testkit % Test)
  .configs(Config.CustomIntegrationTest)
  .settings(Config.testSettings: _*)
  .settings(
    name := "dsl",
    sbtrelease.ReleasePlugin.autoImport.releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    libraryDependencies ++= Seq("com.google.guava" % "guava" % "23.0", "com.typesafe" % "config" % "1.4.2")
  )
//  .settings(excludeDependencies ++= Seq(ExclusionRule("org.apache.pekko")))

lazy val testkit = (project in file("testkit"))
  .dependsOn(client)
  .settings(
    name := "testkit",
    sbtrelease.ReleasePlugin.autoImport.releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    libraryDependencies ++= Build.testDependencies
  )
