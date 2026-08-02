name         := "sbt-codeartifact"
organization := "io.github.regiskuckaertz"

sbtPlugin    := true
scalaVersion := "3.8.4"

val awsVersion = "2.28.0"

lazy val excludeUnusedHttpClients: ModuleID => ModuleID =
  _.exclude("software.amazon.awssdk", "apache-client")
   .exclude("software.amazon.awssdk", "netty-nio-client")

libraryDependencies ++= Seq(
  excludeUnusedHttpClients("software.amazon.awssdk" % "codeartifact"          % awsVersion),
  excludeUnusedHttpClients("software.amazon.awssdk" % "sso"                   % awsVersion),
  excludeUnusedHttpClients("software.amazon.awssdk" % "ssooidc"               % awsVersion),
  "software.amazon.awssdk" % "url-connection-client" % awsVersion,
)

enablePlugins(ScriptedPlugin)
scriptedLaunchOpts ++= Seq("-Xmx1024M", s"-Dplugin.version=${version.value}")
scriptedBufferLog := false

publishMavenStyle := true
licenses  := Seq("Apache-2.0" -> uri("https://www.apache.org/licenses/LICENSE-2.0"))
homepage  := Some(uri("https://github.com/regiskuckaertz/sbt-codeartifact"))
scmInfo   := Some(ScmInfo(
  uri("https://github.com/regiskuckaertz/sbt-codeartifact"),
  "scm:git@github.com:regiskuckaertz/sbt-codeartifact.git"
))
developers := List(Developer(
  "regiskuckaertz", "Regis Kuckaertz",
  "platen-porter0e@icloud.com",
  uri("https://github.com/regiskuckaertz")
))
