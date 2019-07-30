package ru.capjack.gradle.bintray

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class CapjackBintrayPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.pluginManager.apply(BintrayPlugin::class.java)
		project.pluginManager.apply(MavenPublishPlugin::class.java)
		
		project.extensions.create(CapjackBintrayExtension::class.java, "capjackBintray", CapjackBintrayExtensionImpl::class.java, project)
		
		project.afterEvaluate(::configure)
	}
	
	private fun configure(project: Project) {
		val ext = project.extensions.getByType<CapjackBintrayExtension>()
		val publications = project.extensions.getByType<PublishingExtension>().publications
		val publicationNames = mutableSetOf<String>()
		
		if (ext.publications.isEmpty()) {
			publicationNames.addAll(publications.map { it.name })
		}
		else {
			ext.publications.forEach { name ->
				if (publications.findByName(name) != null) {
					publicationNames.add(name)
				}
				else {
					throw UnknownDomainObjectException("Publication '$name' not found")
				}
			}
		}
		
		project.configure<BintrayExtension> {
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
				
				version.name = project.version.toString()
				
				defineLicense(project)?.also { setLicenses(it) }
			}
		}
		
		project.tasks.withType<BintrayUploadTask> {
			val task = this
			doFirst {
				task.publications.mapNotNull {
					when (it) {
						is String                   -> project.extensions.getByType<PublishingExtension>().publications.findByName(it) as? MavenPublicationInternal
						is MavenPublicationInternal -> it
						else                        -> null
					}
				}.forEach {
					it.publishableArtifacts.forEach { a ->
						if (a.extension == "module") {
							it.artifact(a)
						}
					}
				}
			}
		}
	}
	
	private fun Project.whenEvaluated(fn: () -> Unit) {
		if (state.executed) fn()
		else afterEvaluate { fn() }
	}
	
	private fun defineLicense(project: Project): String? {
		return project.rootProject.file("LICENSE").takeIf { it.isFile }?.useLines { lines ->
			for (line in lines) when {
				line.contains("Apache License") -> return "Apache-2.0"
				line.contains("MIT License")    -> return "MIT"
			}
			return null
		}
	}
}