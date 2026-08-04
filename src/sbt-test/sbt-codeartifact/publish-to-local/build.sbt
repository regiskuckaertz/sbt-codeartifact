import software.amazon.awssdk.regions.Region

scalaVersion := "3.8.4"

ThisBuild / codeArtifactDomain      := "my-domain"
ThisBuild / codeArtifactDomainOwner := "123456789012"
ThisBuild / codeArtifactRegion      := Region.EU_WEST_1
ThisBuild / codeArtifactRepository  := "my-repo"

// Bypass real AWS call
ThisBuild / codeArtifactToken := Some("fake-token")

versionScheme := Some("early-semver")

publishTo := Some(Resolver.mavenLocal)

lazy val assertPublished = taskKey[Unit]("Verify the artifact was published to Maven local")
assertPublished := Def.uncached {
  val pom = file(System.getProperty("user.home")) / ".m2" / "repository" /
    "com" / "example" / "publish-to-local-test_3" / "0.1.0" /
    "publish-to-local-test_3-0.1.0.pom"
  assert(pom.exists(), s"Expected POM at $pom but it was not found")
}

name         := "publish-to-local-test"
organization := "com.example"
version      := "0.1.0"
