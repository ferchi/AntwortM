package edu.itq.antwort.Activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.analytics.FirebaseAnalytics
import edu.itq.antwort.R

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        // Analytics Event
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integración completa")
        analytics.logEvent("InitScreen", bundle)

        // Setup
        setup()

    }//fun onCreate

    private fun setup(){

        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed({

            val intent = Intent(this, Login::class.java)
            startActivity(intent)

        }, 1000)

    }//fun setup

}//class MainActivity