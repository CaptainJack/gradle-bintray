package ru.capjack.gradle.bintray

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class CapjackBintrayPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		val rootProject = project.rootProject
		
		if (project != rootProject) {
			project.logger.error("Plugin ru.capjack.capjack-bintray is applicable only to root project")
			return
		}
		
		project.pluginManager.apply(BintrayPlugin::class.java)
		project.pluginManager.apply(MavenPublishPlugin::class.java)
		
		project.extensions.create(CapjackBintrayExtension::class.java, "capjackBintray", CapjackBintrayExtensionImpl::class.java, project)
		
		project.afterEvaluate(::configure)
	}
	
	private fun configure(rootProject: Project) {
		val ext = rootProject.extensions.getByType<CapjackBintrayExtension>()
		val requiredPublications = ext.publications.toMutableSet()
		val publications = rootProject.extensions.getByType<PublishingExtension>().publications
		val publicationNames = mutableSetOf<String>()
		
		if (requiredPublications.isEmpty()) {
			requiredPublications.add(":")
		}
		
		requiredPublications.forEach { name ->
			if (publications.findByName(name) != null) {
				publicationNames.add(name)
			}
			else if (name.startsWith(':')) {
				val project = rootProject.project(name)
				val kmp = project.extensions.findByType<KotlinMultiplatformExtension>()
				if (kmp == null) {
					val publication = project.name
						.split('_', '-')
						.joinToString("", transform = String::capitalize)
						.decapitalize()
					publicationNames.add(publication)
					providePublication(publications, project, publication)
				}
				else {
					publicationNames.addAll(project.extensions.getByType<PublishingExtension>().publications.map { it.name })
				}
				
			}
			else {
				throw UnknownDomainObjectException("Publication '$name' not found")
			}
		}
		
		rootProject.configure<BintrayExtension> {
			val githubRepository = ext.github.let {
				if (it.contains('/')) it
				else "CaptainJack/$it"
			}
			val githubUrl = "https://github.com/$githubRepository"
			
			user = ext.user
			key = ext.key
			
			setPublications(*publicationNames.toTypedArray())
			
			pkg.apply {
				name = ext.name
				userOrg = "capjack"
				repo = ext.repository
				githubRepo = githubRepository
				websiteUrl = githubUrl
				issueTrackerUrl = "$githubUrl/issues"
				vcsUrl = "$githubUrl.git"
				publish = ext.publish
				
				version.name = rootProject.version.toString()
				
				defineLicense(rootProject)?.also { setLicenses(it) }
			}
		}
	}
	
	private fun providePublication(publications: PublicationContainer, project: Project, name: String) {
		project.whenEvaluated {
			if (project.pluginManager.hasPlugin("org.gradle.java")) {
				publications.create<MavenPublication>(name) {
					artifactId = project.name
					groupId = project.group.toString()
					version = project.version.toString()
					
					from(project.components["java"])
					project.tasks.findByName("sourcesJar")?.also {
						artifact(it)
					}
					
				}
			}
			else {
				project.logger.error("Can't provide publication, because project '${project.name}' is not have java component")
			}
		}
	}
	
	private fun Project.whenEvaluated(fn: () -> Unit) {
		if (state.executed) fn()
		else afterEvaluate { fn() }
	}
	
	private fun defineLicense(project: Project): String? {
		return project.file("LICENSE").takeIf { it.isFile }?.useLines { lines ->
			for (line in lines) when {
				line.contains("Apache License") -> return "Apache-2.0"
				line.contains("MIT License")    -> return "MIT"
			}
			return null
		}
	}
}