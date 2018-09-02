package ru.capjack.gradle.capjack.bintray

interface CapjackBintrayExtension {
	var github: String
	var publications: List<String>
	var repository: String
	var publish: Boolean
	var user: String?
	var key: String?
}