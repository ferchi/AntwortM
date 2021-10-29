package edu.itq.antwort.Classes

import kotlin.collections.ArrayList

data class Questions(

    val author: String = "",
    val name: String = "",
    val description: String = "",
    val title: String = "",
    var id: String = "",
    val answers: Int = 0,
    val date: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val likes: ArrayList<String> = ArrayList(),
    val dislikes: ArrayList<String> = ArrayList()

    )//class
