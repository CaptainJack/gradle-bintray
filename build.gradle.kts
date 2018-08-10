import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.2.51"
	`kotlin-dsl`
	`java-gradle-plugin`
	id("com.gradle.plugin-publish") version "0.10.0"
	id("nebula.release") version "6.3.5"
}

group = "ru.capjack.gradle"

repositories {
	jcenter()
}

dependencies {
	implementation(kotlin("stdlib"))
	implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
}

gradlePlugin {
	(plugins) {
		"CapjackPublish" {
			id = "ru.capjack.capjack-publish"
			implementationClass = "ru.capjack.gradle.capjackPublish.CapjackPublishPlugin"
		}
	}
}

pluginBundle {
	vcsUrl = "https://github.com/CaptainJack/gradle-capjack-publish"
	website = vcsUrl
	description = "Provides publishing of artifacts to CaptainJack Bintray repository"
	tags = listOf("capjack")
	
	plugins["CapjackPublish"].displayName = "CapjackPublish plugin"
}

tasks.getByName("postRelease").dependsOn("publishPlugins")