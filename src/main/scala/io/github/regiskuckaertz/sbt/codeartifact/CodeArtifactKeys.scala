package io.github.regiskuckaertz.sbt.codeartifact

import sbt.*

trait CodeArtifactKeys {
  val codeArtifactDomain:      SettingKey[Option[String]]
  val codeArtifactDomainOwner: SettingKey[Option[String]]
  val codeArtifactRegion:      SettingKey[Option[String]]
  val codeArtifactRepository:  SettingKey[Option[String]]
  val codeArtifactToken:       TaskKey[Option[String]]
}
