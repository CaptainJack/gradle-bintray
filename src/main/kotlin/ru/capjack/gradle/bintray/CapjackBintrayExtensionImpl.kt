package ru.capjack.gradle.bintray

import org.gradle.api.Project

open class CapjackBintrayExtensionImpl(project: Project) : CapjackBintrayExtension {
	override var name: String = project.rootProject.name
	override var github: String = project.rootDir.name
	override var publications: List<String> = emptyList()
	
	override var repository: String = "public"
	override var publish: Boolean = true
	override var user: String? = project.findProperty("capjack.bintray.user") as String?
	override var key: String? = project.findProperty("capjack.bintray.key") as String?
	
	override fun publications(vararg names: String) {
		publications = names.toList()
	}
}
