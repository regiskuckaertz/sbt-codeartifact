package io.github.regiskuckaertz.sbt.codeartifact

import sbt.*
import sbt.Keys.*
import scala.concurrent.duration.*
import sjsonnew.*
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.codeartifact.CodeartifactClient
import software.amazon.awssdk.services.codeartifact.model.{CodeartifactException, GetAuthorizationTokenRequest}

object CodeArtifactPlugin extends AutoPlugin {
  // SBT 2 caches heavily but to benefit from this, one needs to be able to hash
  // the input keys. The easiest way to achieve this is to provide a JSON codec.
  given JsonFormat[Region]:
    def write[J](region: Region, builder: Builder[J]): Unit =
      builder.writeString(region.toString())
    def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): Region =
      jsOpt match {
        case Some(js) => Region.of(unbuilder.readString(js))
        case None => Region.EU_WEST_2
      }

  given JsonFormat[FiniteDuration]:
    def write[J](duration: FiniteDuration, builder: Builder[J]): Unit =
      builder.writeLong(duration.toSeconds)
    def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): FiniteDuration =
      jsOpt match {
        case Some(js) => FiniteDuration(unbuilder.readLong(js), "seconds")
        case None => Duration.Zero
      }

  object autoImport extends CodeArtifactKeys {
    override val codeArtifactDomain: SettingKey[String] =
      settingKey("AWS CodeArtifact domain name")
    override val codeArtifactDomainOwner: SettingKey[String] =
      settingKey("AWS account ID that owns the CodeArtifact domain")
    override val codeArtifactRegion: SettingKey[Region] =
      settingKey("AWS region where the CodeArtifact domain is hosted")
    override val codeArtifactRepository: SettingKey[String] =
      settingKey("AWS CodeArtifact repository name")
    override val codeArtifactTokenDuration: SettingKey[FiniteDuration] =
      settingKey("AWS CodeArtifact token duration")
    override val codeArtifactToken: TaskKey[Option[String]] =
      taskKey("Fetches a bearer token; checks CODEARTIFACT_AUTH_TOKEN env var first")
  }

  import autoImport.*

  override def trigger: PluginTrigger = allRequirements

  override def buildSettings: Seq[Setting[?]] = Seq(
    codeArtifactDomain        := "<domain>",
    codeArtifactDomainOwner   := "<account-id>",
    codeArtifactRegion        := Region.EU_WEST_2,
    codeArtifactRepository    := "<repository>",
    codeArtifactTokenDuration := 7.hours,
    codeArtifactToken         := fetchToken(
      codeArtifactDomain.value,
      codeArtifactDomainOwner.value,
      codeArtifactRegion.value,
      codeArtifactTokenDuration.value,
      streams.value.log
    ),
    resolvers ++= {
      val domain = codeArtifactDomain.value
      val owner  = codeArtifactDomainOwner.value
      val region = codeArtifactRegion.value
      val repo   = codeArtifactRepository.value
      val url = s"https://$domain-$owner.d.codeartifact.$region.amazonaws.com/maven/$repo/"
      Seq(s"$domain/$repo" at url)
    },
    credentials ++= {
      val domain = codeArtifactDomain.value
      val owner  = codeArtifactDomainOwner.value
      val region = codeArtifactRegion.value
      val token  = codeArtifactToken.value
      val host = s"$domain-$owner.d.codeartifact.$region.amazonaws.com"
      token.map(token => Credentials("$domain/$repo", host, "aws", token)).toSeq
    },
    publishTo := {
      val domain = codeArtifactDomain.value
      val owner  = codeArtifactDomainOwner.value
      val region = codeArtifactRegion.value
      val repo   = codeArtifactRepository.value
      val url = s"https://$domain-$owner.d.codeartifact.$region.amazonaws.com/maven/$repo/"
      Some(s"$domain/$repo" at url)
    },
    publishMavenStyle := true
  )

  private def fetchToken(
    domain: String,
    domainOwner: String,
    region: Region,
    duration: FiniteDuration,
    log: sbt.util.Logger
  ): Option[String] =
    Option(System.getenv("CODEARTIFACT_AUTH_TOKEN"))
      .filter(_.nonEmpty)
      .orElse {
        log.info(s"Fetching CodeArtifact token for domain '$domain' in region '$region'...")
        val client = CodeartifactClient.builder()
          .region(region)
          .credentialsProvider(DefaultCredentialsProvider.create())
          .httpClientBuilder(UrlConnectionHttpClient.builder())
          .build()
        try {
          val request = GetAuthorizationTokenRequest.builder()
            .domain(domain)
            .domainOwner(domainOwner)
            .durationSeconds(duration.toSeconds)
            .build()
          Some(client.getAuthorizationToken(request).authorizationToken())
        } catch {
          case e: SdkClientException =>
            throw new MessageOnlyException(
              s"Failed to get CodeArtifact token: ${e.getMessage}\nHint: run `aws sso login`"
            )
          case e: CodeartifactException if e.statusCode() == 401 || e.statusCode() == 403 =>
            throw new MessageOnlyException(
              s"CodeArtifact authorization failed: ensure the IAM principal has " +
              s"`codeartifact:GetAuthorizationToken` permission. ${e.getMessage}"
            )
          case e: CodeartifactException =>
            throw new MessageOnlyException(
              s"CodeArtifact error (HTTP ${e.statusCode()}): ${e.getMessage}"
            )
        } finally {
          client.close()
        }
      }
}
