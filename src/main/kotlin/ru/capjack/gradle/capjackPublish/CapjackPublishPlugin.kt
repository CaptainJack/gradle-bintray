package ru.capjack.gradle.capjackPublish

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

class CapjackPublishPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.pluginManager.apply(MavenPublishPlugin::class.java)
		project.pluginManager.apply(BintrayPlugin::class.java)
		
		project.extensions.create(CapjackPublishExtension::class.java, "capjackPublish", CapjackPublishExtensionImpl::class.java, project)
		
		project.afterEvaluate(::configure)
	}
	
	private fun configure(project: Project) {
		val cj = project.extensions.getByType(CapjackPublishExtension::class.java)
		val publications = project.extensions.getByType(PublishingExtension::class.java).publications
		
		val publication = cj.publication
			?: (publications.find { !it.name.endsWith("PluginMarkerMaven") }?.name)
			?: project.name.split(Regex("[-_ ]")).joinToString("") { it.capitalize() }
		
		
		if (null == publications.findByName(publication) && project.plugins.hasPlugin(JavaPlugin::class.java)) {
			publications.create(publication, MavenPublication::class.java) {
				from(project.components["java"])
				project.tasks.findByName("sourcesJar")?.also { artifact(it) }
			}
		}
		
		project.extensions.getByType(BintrayExtension::class.java).apply {
			val repository = cj.githubRepository.let {
				if (it.contains('/')) it
				else "CaptainJack/$it"
			}
			val githubUrl = "https://github.com/$repository"
			
			user = cj.bintrayUser
			key = cj.bintrayKey
			
			setPublications(publication)
			
			pkg.apply {
				name = project.name
				userOrg = "capjack"
				repo = cj.bintrayRepository
				githubRepo = repository
				websiteUrl = githubUrl
				issueTrackerUrl = "$githubUrl/issues"
				vcsUrl = "$githubUrl.git"
				
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
		
		return file?.run {
			useLines { lines ->
				if (lines.any { it.contains("Apache License") }) "Apache-2.0"
				else null
			}
		}
	}
	
}