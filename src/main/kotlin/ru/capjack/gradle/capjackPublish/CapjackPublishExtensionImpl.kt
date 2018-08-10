package ru.capjack.gradle.capjackPublish

import org.gradle.api.Project

class CapjackPublishExtensionImpl(project: Project) : CapjackPublishExtension {
	override var githubRepository: String = project.name
	override var publication: String = "lib"
	
	override var bintrayRepository: String = "maven"
	override var bintrayUser: String? = project.findProperty("capjack.bintrayUser") as String?
	override var bintrayKey: String? = project.findProperty("capjack.bintrayKey") as String?
}