package edu.itq.antwort.Classes

data class Users(
    val email: String = "",
    val name: String = "",
    val rol: String = "",
    val imgProfile: String = "",
    var token: String = "",
    var questions: Int = 0,
    var answers: Int = 0,
    var topics: ArrayList<String> = ArrayList(),
    var updated: Boolean = false
)