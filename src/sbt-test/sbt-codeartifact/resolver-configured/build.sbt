import software.amazon.awssdk.regions.Region

scalaVersion := "3.8.4"

ThisBuild / codeArtifactDomain      := "my-domain"
ThisBuild / codeArtifactDomainOwner := "123456789012"
ThisBuild / codeArtifactRegion      := Region.EU_WEST_1
ThisBuild / codeArtifactRepository  := "my-repo"

// Bypass real AWS call
ThisBuild / codeArtifactToken := Some("fake-token")

lazy val assertResolver = taskKey[Unit]("Verify the CodeArtifact resolver URL is correctly configured")

assertResolver := {
  val expectedName = "my-domain/my-repo"
  val found = resolvers.value.exists(_.name == expectedName)
  assert(found, s"Expected resolver named '$expectedName' not found; got: ${resolvers.value.map(_.name)}")
}
