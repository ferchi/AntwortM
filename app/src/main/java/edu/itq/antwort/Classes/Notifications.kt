package edu.itq.antwort.Classes

data class Notifications(

    val id: String = "",
    val title: String = "",
    val question: String = "",
    val author: String = "",
    val content: String = "",
    val user: String = "",
    val date: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()

)//class Notifications
