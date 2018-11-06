package ru.capjack.gradle.capjack.bintray

interface CapjackBintrayExtension {
	val name: String
	var github: String
	var publications: List<String>
	var repository: String
	var publish: Boolean
	var user: String?
	var key: String?
	
	fun publications(vararg names: String)
}