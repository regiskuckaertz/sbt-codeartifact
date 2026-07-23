package com.pirum.sbt.codeartifact

import sbt.*
import sbt.Keys.*
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.codeartifact.CodeartifactClient
import software.amazon.awssdk.services.codeartifact.model.{CodeartifactException, GetAuthorizationTokenRequest}

object CodeArtifactPlugin extends AutoPlugin {

  object autoImport extends CodeArtifactKeys {
    override val codeArtifactDomain: SettingKey[Option[String]] =
      settingKey("AWS CodeArtifact domain name")
    override val codeArtifactDomainOwner: SettingKey[Option[String]] =
      settingKey("AWS account ID that owns the CodeArtifact domain")
    override val codeArtifactRegion: SettingKey[Option[String]] =
      settingKey("AWS region where the CodeArtifact domain is hosted (default: eu-west-2)")
    override val codeArtifactRepository: SettingKey[Option[String]] =
      settingKey("AWS CodeArtifact repository name")
    override val codeArtifactToken: TaskKey[Option[String]] =
      taskKey("Fetches a 12-hour bearer token; checks CODEARTIFACT_AUTH_TOKEN env var first")
  }

  import autoImport.*

  override def trigger: PluginTrigger = allRequirements

  override def globalSettings: Seq[Setting[?]] = Seq(
    codeArtifactDomain      := None,
    codeArtifactDomainOwner := None,
    codeArtifactRegion      := Some("eu-west-2"),
    codeArtifactRepository  := None,
    codeArtifactToken       := fetchToken(
      codeArtifactDomain.value,
      codeArtifactDomainOwner.value,
      codeArtifactRegion.value,
      streams.value.log
    )
  )

  override def projectSettings: Seq[Setting[?]] = Seq(
    resolvers ++= {
      (for {
        domain <- codeArtifactDomain.value
        owner  <- codeArtifactDomainOwner.value
        region <- codeArtifactRegion.value
        repo   <- codeArtifactRepository.value
      } yield {
        val url = s"https://$domain-$owner.d.codeartifact.$region.amazonaws.com/maven/$repo/"
        s"CodeArtifact[$repo]" at url
      }).toSeq
    },
    credentials ++= {
      (for {
        domain <- codeArtifactDomain.value
        owner  <- codeArtifactDomainOwner.value
        region <- codeArtifactRegion.value
        token  <- codeArtifactToken.value
      } yield {
        val host = s"$domain-$owner.d.codeartifact.$region.amazonaws.com"
        Credentials("AWS CodeArtifact", host, "aws", token)
      }).toSeq
    },
    publishTo := {
      for {
        domain <- codeArtifactDomain.value
        owner  <- codeArtifactDomainOwner.value
        region <- codeArtifactRegion.value
        repo   <- codeArtifactRepository.value
      } yield {
        val url = s"https://$domain-$owner.d.codeartifact.$region.amazonaws.com/maven/$repo/"
        s"CodeArtifact[$repo]" at url
      }
    },
    publishMavenStyle := codeArtifactRepository.value.isDefined
  )

  private def fetchToken(
    domain: Option[String],
    domainOwner: Option[String],
    region: Option[String],
    log: sbt.util.Logger
  ): Option[String] =
    (domain, domainOwner) match {
      case (Some(d), Some(owner)) =>
        Option(System.getenv("CODEARTIFACT_AUTH_TOKEN"))
          .filter(_.nonEmpty)
          .orElse {
            val r = region.getOrElse("eu-west-2")
            log.info(s"Fetching CodeArtifact token for domain '$d' in region '$r'...")
            val client = CodeartifactClient.builder()
              .region(Region.of(r))
              .credentialsProvider(DefaultCredentialsProvider.create())
              .httpClientBuilder(UrlConnectionHttpClient.builder())
              .build()
            try {
              val request = GetAuthorizationTokenRequest.builder()
                .domain(d)
                .domainOwner(owner)
                .durationSeconds(43200L)
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
      case _ => None
    }
}
