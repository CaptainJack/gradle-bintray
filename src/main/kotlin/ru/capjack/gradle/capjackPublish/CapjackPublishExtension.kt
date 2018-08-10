package ru.capjack.gradle.capjackPublish

interface CapjackPublishExtension {
	var githubRepository: String
	var publication: String?
	var bintrayRepository: String
	var bintrayUser: String?
	var bintrayKey: String?
}