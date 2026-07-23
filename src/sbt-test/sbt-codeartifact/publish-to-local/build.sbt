scalaVersion := "3.8.4"

codeArtifactDomain      := Some("my-domain")
codeArtifactDomainOwner := Some("123456789012")
codeArtifactRegion      := Some("eu-west-2")
codeArtifactRepository  := Some("my-repo")

// Bypass real AWS call
codeArtifactToken := Some("fake-token")

versionScheme := Some("early-semver")

// Redirect publishing to a local directory so no network is needed
publishTo := Some(Resolver.file("test-local-repo", baseDirectory.value / "local-repo")(using Resolver.mavenStylePatterns))

name         := "publish-to-local-test"
organization := "com.example"
version      := "0.1.0"
scalaVersion := "3.8.4"
