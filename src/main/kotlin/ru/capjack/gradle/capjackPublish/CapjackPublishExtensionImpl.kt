package ru.capjack.gradle.capjackPublish

import org.gradle.api.Project

open class CapjackPublishExtensionImpl(project: Project) : CapjackPublishExtension {
	override var githubRepository: String = project.name
	override var publication: String? = null
	
	override var bintrayRepository: String = "maven"
	override var bintrayPublish: Boolean = true
	override var bintrayUser: String? = project.findProperty("capjack.bintrayUser") as String?
	override var bintrayKey: String? = project.findProperty("capjack.bintrayKey") as String?
}