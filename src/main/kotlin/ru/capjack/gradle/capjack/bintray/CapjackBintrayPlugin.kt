package ru.capjack.gradle.capjack.bintray

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
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
		val publications = rootProject.extensions.getByType<PublishingExtension>().publications
		val publicationNames = ext.publications.toMutableSet()
		
		if (publicationNames.isEmpty() || publicationNames.contains("*")) {
			publicationNames.remove("*")
			val kmp = rootProject.extensions.findByType<KotlinMultiplatformExtension>()
			if (kmp == null) {
				publicationNames.add(rootProject.name)
			}
			else {
				publicationNames.addAll(
					kmp.targets.filter { it.publishable }.map { it.name }
				)
			}
		}
		
		val projects = rootProject.allprojects
		
		publicationNames.filter { publications.findByName(it) == null }.forEach { name ->
			projects.find { it.name == name }
				?.also { providePublication(publications, it) }
				?: rootProject.logger.error("Project for publication '$name' not found")
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
	
	private fun providePublication(publications: PublicationContainer, project: Project) {
		project.whenEvaluated {
			if (project.pluginManager.hasPlugin("org.gradle.java")) {
				publications.create<MavenPublication>(project.name) {
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
	
	private fun <T> Project.whenEvaluated(fn: () -> Unit) {
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