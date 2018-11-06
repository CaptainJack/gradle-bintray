package ru.capjack.gradle.capjack.bintray

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.get
import java.io.File

class CapjackBintrayPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.pluginManager.apply(MavenPublishPlugin::class.java)
		project.pluginManager.apply(BintrayPlugin::class.java)
		
		project.extensions.create(CapjackBintrayExtension::class.java, "capjackBintray", CapjackBintrayExtensionImpl::class.java, project)
		
		project.afterEvaluate(::configure)
	}
	
	private fun configure(project: Project) {
		val ext = project.extensions.getByType(CapjackBintrayExtension::class.java)
		val publishingPublications = project.extensions.getByType(PublishingExtension::class.java).publications
		
		val publications = ext.publications.takeIf { it.isNotEmpty() }
			?: listOf(project.name.split(Regex("[-_ ]")).joinToString("") { it.capitalize() })
		
		
		if (publishingPublications.none { publications.contains(it.name) } && project.plugins.hasPlugin(JavaPlugin::class.java)) {
			publishingPublications.create(publications[0], MavenPublication::class.java) {
				from(project.components["java"])
				project.tasks.findByName("sourcesJar")?.also { artifact(it) }
			}
		}
		
		project.extensions.getByType(BintrayExtension::class.java).apply {
			val githubRepository = ext.github.let {
				if (it.contains('/')) it
				else "CaptainJack/$it"
			}
			val githubUrl = "https://github.com/$githubRepository"
			
			user = ext.user
			key = ext.key
			
			setPublications(*publications.toTypedArray())
			
			pkg.apply {
				name = ext.name
				userOrg = "capjack"
				repo = ext.repository
				githubRepo = githubRepository
				websiteUrl = githubUrl
				issueTrackerUrl = "$githubUrl/issues"
				vcsUrl = "$githubUrl.git"
				publish = ext.publish
				
				version.name = project.version.toString()
				
				defineLicense(project)?.also { setLicenses(it) }
			}
		}
	}
	
	
	private fun defineLicense(project: Project): String? {
		var p: Project? = project
		var file: File? = null
		
		while (p != null) {
			val f = p.file("LICENSE")
			if (f.isFile) {
				file = f
				break
			}
			else p = p.parent
		}
		
		file?.apply {
			useLines { lines ->
				lines.forEach { line ->
					when {
						line.contains("Apache License") -> return "Apache-2.0"
						line.contains("MIT License")    -> return "MIT"
					}
				}
			}
		}
		return null
	}
	
}