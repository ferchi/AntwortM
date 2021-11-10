package edu.itq.antwort

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import co.nedim.maildroidx.MaildroidX
import co.nedim.maildroidx.MaildroidXType


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

    fun sendEmail(destino:String, subject:String, body:String, context: FragmentActivity){
        MaildroidX.Builder()
            .smtp("smtp.gmail.com")
            .smtpUsername("antwort.notifications@gmail.com")
            .smtpPassword("memomemo")
            .port("465")
            .type(MaildroidXType.HTML)
            .to(destino)
            .from("antwort.notifications@gmail.com")
            .subject(subject)
            .body(body)
            .onCompleteCallback(object : MaildroidX.onCompleteCallback{
                override val timeout: Long = 3000
                override fun onSuccess() {
                    customToast(context,"Solicitado con éxito")
                }
                override fun onFail(errorMessage: String) {
                    customToast(context,"Revisa tu conexión a internet")
                }
            })
            .mail()
    }

    class LinearLayoutManagerWrapper : LinearLayoutManager {
        constructor(context: Context?) : super(context) {}
        constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(
            context,
            orientation,
            reverseLayout
        ) {
        }

        constructor(
            context: Context?,
            attrs: AttributeSet?,
            defStyleAttr: Int,
            defStyleRes: Int
        ) : super(context, attrs, defStyleAttr, defStyleRes) {
        }

        override fun supportsPredictiveItemAnimations(): Boolean {
            return false
        }
    }
}