package ru.capjack.gradle.capjack.bintray

interface CapjackBintrayExtension {
	var name: String
	var github: String
	var publications: List<String>
	var repository: String
	var publish: Boolean
	var user: String?
	var key: String?
	
	fun publications(vararg names: String)
}