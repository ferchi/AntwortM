package edu.itq.antwort.Classes

import android.os.Build

import kotlin.collections.ArrayList

data class Questions constructor(

    val author: String = "",
    val name: String = "",
    val description: String = "",
    val title: String = "",
    var id: String = "",
    val date: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val likes: ArrayList<String> = ArrayList(),
    val dislikes: ArrayList<String> = ArrayList()

    )//class
