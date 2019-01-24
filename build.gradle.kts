plugins {
	`kotlin-dsl`
	`java-gradle-plugin`
	`maven-publish`
	id("com.gradle.plugin-publish") version "0.10.0"
	id("nebula.release") version "9.0.0"
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
		id = "ru.capjack.capjack-bintray"
		implementationClass = "ru.capjack.gradle.capjack.bintray.CapjackBintrayPlugin"
		displayName = "CapjackBintray"
	}
}

pluginBundle {
	vcsUrl = "https://github.com/CaptainJack/gradle-capjack-bintray"
	website = vcsUrl
	description = "Provides publishing of artifacts to CaptainJack Bintray repository"
	tags = listOf("capjack")
}

tasks["postRelease"].dependsOn("publishPlugins")