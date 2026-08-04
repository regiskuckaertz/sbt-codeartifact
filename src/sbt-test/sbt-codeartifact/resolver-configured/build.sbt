import software.amazon.awssdk.regions.Region

scalaVersion := "3.8.4"

ThisBuild / codeArtifactDomain      := "my-domain"
ThisBuild / codeArtifactDomainOwner := "123456789012"
ThisBuild / codeArtifactRegion      := Region.EU_WEST_1
ThisBuild / codeArtifactRepository  := "my-repo"

// Bypass real AWS call
ThisBuild / codeArtifactToken := Some("fake-token")

lazy val assertResolver = taskKey[Unit]("Verify the CodeArtifact resolver URL is correctly configured")
lazy val assertCredentials = taskKey[Unit]("Verify the CodeArtifact credentials are correctly configured")

assertResolver := Def.uncached {
  val expectedName = "my-domain/my-repo"
  val found = resolvers.value.exists(_.name == expectedName)
  assert(found, s"Expected resolver named '$expectedName' not found; got: ${resolvers.value.map(_.name)}")
}

assertCredentials := Def.uncached {
  val expectedRealm = "my-domain/my-repo"
  val expectedUrl = "https://my-domain-123456789012.d.codeartifact.eu-west-1.amazonaws.com/maven/my-repo/"
  val found = credentials.value.exists {
    case creds: Credentials.DirectCredentials => creds.realm == expectedRealm && creds.host == expectedUrl
    case _ => false
  }
}
