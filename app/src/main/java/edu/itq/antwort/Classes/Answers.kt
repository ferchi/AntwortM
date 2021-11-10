package edu.itq.antwort.Classes

data class Answers(

    val author: String = "",
    val nameAuthor: String = "",
    val content: String = "",
    var id: String = "",
    val date: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val question: String = "",
    val verified: Boolean = false,
    val likes: ArrayList<String> = ArrayList(),
    val dislikes: ArrayList<String> = ArrayList(),
    val edited: Boolean = false

)