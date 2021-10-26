package edu.itq.antwort

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.FragmentActivity

object Methods {

    fun getEmail(context:FragmentActivity) : String?{

        val prefs : SharedPreferences =  context.getSharedPreferences(context.getString(R.string.prefs_file), Context.MODE_PRIVATE)
        return prefs.getString("email", null)

    }//getMail
}