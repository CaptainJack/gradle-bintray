import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
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
	implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
}

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }

tasks["postRelease"].dependsOn("publishPlugins")

gradlePlugin {
	plugins.create("CapjackBintray") {
		id = "ru.capjack.capjack-bintray"
		implementationClass = "ru.capjack.gradle.capjack.bintray.CapjackBintrayPlugin"
	}
}

pluginBundle {
	vcsUrl = "https://github.com/CaptainJack/gradle-capjack-bintray"
	website = vcsUrl
	description = "Provides publishing of artifacts to CaptainJack Bintray repository"
	tags = listOf("capjack")
	
	plugins["CapjackBintray"].displayName = "CapJack Bintray plugin"
}