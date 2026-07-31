package io.github.regiskuckaertz.sbt.codeartifact

import scala.concurrent.duration.FiniteDuration
import sbt.*
import software.amazon.awssdk.regions.Region

trait CodeArtifactKeys {
  val codeArtifactDomain:        SettingKey[String]
  val codeArtifactDomainOwner:   SettingKey[String]
  val codeArtifactRegion:        SettingKey[Region]
  val codeArtifactRepository:    SettingKey[String]
  val codeArtifactTokenDuration: SettingKey[FiniteDuration]
  val codeArtifactToken:       TaskKey[Option[String]]
}
