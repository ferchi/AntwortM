package edu.itq.antwort

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.FragmentActivity
import android.widget.TextView
import android.view.View
import android.widget.Toast
import android.view.Gravity
import android.view.ViewGroup
import android.view.LayoutInflater


object Methods {

    fun getEmail(context:FragmentActivity) : String?{

        val prefs : SharedPreferences =  context.getSharedPreferences(context.getString(R.string.prefs_file), Context.MODE_PRIVATE)
        return prefs.getString("email", null)

    }//getMail

    fun customToast(context: FragmentActivity, message:String){
        val inflater: LayoutInflater = context.layoutInflater
        val layout: View = inflater.inflate(
            R.layout.custom_toast,
            context.findViewById(R.id.custom_toast_container) as ViewGroup?
        )

        val text = layout.findViewById<View>(R.id.text_toast) as TextView
        text.text = message

        val toast = Toast(context)
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
        toast.duration = Toast.LENGTH_LONG
        toast.setView(layout)
        toast.show()
    }
}