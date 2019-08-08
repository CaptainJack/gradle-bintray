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
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

@Suppress("UnstableApiUsage")
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
			if (publications.isEmpty()) {
				providePublication(project, publications)?.let(publicationNames::add)
			}
			else {
				publicationNames.addAll(publications.map { it.name })
			}
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
	
	private fun providePublication(project: Project, publications: PublicationContainer): String? {
		val name = project.name.split('_', '-').joinToString("", transform = String::capitalize).decapitalize()
		
		if (project.pluginManager.hasPlugin("org.gradle.java")) {
			
			var sourcesJar = project.tasks.findByName("sourcesJar")
			
			if (sourcesJar == null) {
				sourcesJar = project.tasks.create<Jar>("sourcesJar") {
					archiveClassifier.set("sources")
					from(project.extensions.getByName<SourceSetContainer>("sourceSets")["main"].allSource)
				}
			}
			
			publications.create<MavenPublication>(name) {
				artifactId = project.name
				groupId = project.group.toString()
				version = project.version.toString()
				
				from(project.components["java"])
				artifact(sourcesJar)
			}
		}
		
		return null
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