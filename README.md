# sbt-codeartifact

An sbt AutoPlugin that transparently wires up resolution and publishing for a private AWS CodeArtifact Maven repository. It works out of the box for local developers using AWS IAM Identity Center (`aws sso login`) and in CI using environment-variable or instance-profile credentials.

## Requirements

- sbt 2.x
- AWS account with a CodeArtifact domain and repository

## Installation

In `project/plugins.sbt`:

```scala
addSbtPlugin("io.github.regiskuckaertz" % "sbt-codeartifact" % "<version>")
```

## Configuration

Add the following to your root `build.sbt`. All four settings are required; configuring them in global scope ensures every sub-project picks them up automatically.

```scala
codeArtifactDomain      := "acme-artifacts"
codeArtifactDomainOwner := "123456789012"   // AWS account ID
codeArtifactRegion      := Region.EU_WEST_2 // optional, this is the default
codeArtifactRepository  := "internal"
```

The plugin auto-activates on every JVM project. Until these settings are configured the plugin is a no-op.

To opt a sub-project out:

```scala
lazy val root = project.disablePlugins(CodeArtifactPlugin)
```

## How it works

On the first task that needs credentials (e.g. `update` or `publish`), the plugin:

1. Checks the `CODEARTIFACT_AUTH_TOKEN` environment variable. If set, that value is used directly — no AWS call is made.
2. Otherwise calls `codeartifact:GetAuthorizationToken` via the AWS SDK using the default credential chain (`~/.aws/credentials`, environment variables, instance profile, AWS SSO, …).

The token is a 12-hour bearer token. Because it lives in the sbt task graph in global scope, **it is fetched at most once per sbt session** regardless of how many sub-projects are in the build.

Once the token is available the plugin sets:

| Key | Value |
|---|---|
| `resolvers` | `https://<domain>-<owner>.d.codeartifact.<region>.amazonaws.com/maven/<repo>/` |
| `credentials` | host = same host, user = `aws`, password = token |
| `publishTo` | same URL as the resolver |
| `publishMavenStyle` | `true` |

## Local development

```bash
aws sso login          # refresh your SSO session
sbt update             # token is fetched automatically
```

## CI / CD

Set `CODEARTIFACT_AUTH_TOKEN` before invoking sbt:

```bash
export CODEARTIFACT_AUTH_TOKEN=$(aws codeartifact get-authorization-token \
  --domain acme-artifacts \
  --domain-owner 123456789012 \
  --query authorizationToken \
  --output text)
sbt publish
```

Alternatively, if the CI runner has an IAM instance profile with `codeartifact:GetAuthorizationToken` permission, leave the environment variable unset and the plugin will use the instance profile automatically.

## Error messages

| Situation | Message |
|---|---|
| No credentials found | `Failed to get CodeArtifact token: … Hint: run \`aws sso login\`` |
| 401 / 403 from CodeArtifact | `CodeArtifact authorization failed: ensure the IAM principal has \`codeartifact:GetAuthorizationToken\` permission.` |
| Other HTTP error | `CodeArtifact error (HTTP <status>): <message>` |

## License

Apache 2.0 — see [LICENSE](https://www.apache.org/licenses/LICENSE-2.0).
