package edu.itq.antwort.Classes

data class Answers(

    val author: String = "",
    val nameAuthor: String = "",
    val content: String = "",
    val id: String = "",
    val date: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val question: String = "",
    val likes: ArrayList<String> = ArrayList(),
    val dislikes: ArrayList<String> = ArrayList()

)