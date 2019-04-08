plugins {
	`kotlin-dsl`
	`java-gradle-plugin`
	`maven-publish`
	id("com.gradle.plugin-publish") version "0.10.1"
	id("nebula.release") version "10.0.1"
}

group = "ru.capjack.gradle"

repositories {
	jcenter()
}

dependencies {
	implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
	implementation(kotlin("gradle-plugin"))
}

gradlePlugin {
	plugins.create("CapjackBintray") {
		id = "ru.capjack.bintray"
		implementationClass = "ru.capjack.gradle.bintray.CapjackBintrayPlugin"
		displayName = "CapjackBintray"
	}
}

pluginBundle {
	vcsUrl = "https://github.com/CaptainJack/gradle-bintray"
	website = vcsUrl
	description = "Provides publishing of artifacts to CaptainJack Bintray repository"
	tags = listOf("capjack")
}

tasks["postRelease"].dependsOn("publishPlugins")