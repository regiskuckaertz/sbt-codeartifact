import software.amazon.awssdk.regions.Region

scalaVersion := "3.8.4"

codeArtifactDomain      := "my-domain"
codeArtifactDomainOwner := "123456789012"
codeArtifactRegion      := Region.EU_WEST_1
codeArtifactRepository  := "my-repo"

// Bypass real AWS call
codeArtifactToken := Some("fake-token")

versionScheme := Some("early-semver")

publishTo := Some(Resolver.mavenLocal)

lazy val assertPublished = taskKey[Unit]("Verify the artifact was published to Maven local")
assertPublished := {
  val pom = file(System.getProperty("user.home")) / ".m2" / "repository" /
    "com" / "example" / "publish-to-local-test_3" / "0.1.0" /
    "publish-to-local-test_3-0.1.0.pom"
  assert(pom.exists(), s"Expected POM at $pom but it was not found")
}

name         := "publish-to-local-test"
organization := "com.example"
version      := "0.1.0"
