name         := "sbt-codeartifact"
organization := "io.github.regiskuckaertz"

sbtPlugin    := true
scalaVersion := "3.8.4"

val awsVersion = "2.28.0"

libraryDependencies ++= Seq(
  "software.amazon.awssdk" % "codeartifact"          % awsVersion,
  "software.amazon.awssdk" % "sso"                   % awsVersion,
  "software.amazon.awssdk" % "ssooidc"               % awsVersion,
  "software.amazon.awssdk" % "url-connection-client" % awsVersion,
)

enablePlugins(ScriptedPlugin)
scriptedLaunchOpts ++= Seq("-Xmx1024M", s"-Dplugin.version=${version.value}")
scriptedBufferLog := false

publishMavenStyle := true
licenses  := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))
homepage  := Some(url("https://github.com/regiskuckaertz/sbt-codeartifact"))
scmInfo   := Some(ScmInfo(
  url("https://github.com/regiskuckaertz/sbt-codeartifact"),
  "scm:git@github.com:regiskuckaertz/sbt-codeartifact.git"
))
developers := List(Developer(
  "regiskuckaertz", "Regis Kuckaertz",
  "platen-porter0e@icloud.com",
  url("https://github.com/regiskuckaertz")
))
