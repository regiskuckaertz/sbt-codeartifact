scalaVersion := "3.8.4"

codeArtifactDomain      := Some("my-domain")
codeArtifactDomainOwner := Some("123456789012")
codeArtifactRegion      := Some("eu-west-2")
codeArtifactRepository  := Some("my-repo")

lazy val assertResolver = taskKey[Unit]("Verify the CodeArtifact resolver URL is correctly configured")

assertResolver := {
  val expectedName = "CodeArtifact[my-repo]"
  val found = resolvers.value.exists(_.name == expectedName)
  assert(found, s"Expected resolver named '$expectedName' not found; got: ${resolvers.value.map(_.name)}")
}
