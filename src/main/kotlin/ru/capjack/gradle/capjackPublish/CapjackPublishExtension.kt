package ru.capjack.gradle.capjackPublish

import org.gradle.api.Project

class CapjackPublishExtension(project: Project) {
	
	var githubRepository: String = project.name
	var publication: String = "lib"
	
	var bintrayRepository: String = "maven"
	var bintrayUser: String? = project.findProperty("capjack.bintrayUser") as String?
	var bintrayKey: String? = project.findProperty("capjack.bintrayKey") as String?
}