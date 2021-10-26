package edu.itq.antwort.Classes

data class Users(
    val email: String = "",
    val name: String = "",
    val rol: String = "",
    var token: String = "",
    var questions: Int = 0,
    var answers: Int = 0
)