package edu.itq.antwort.Activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import edu.itq.antwort.R

class ForgotPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Setup
        setup()

    }//fun onCreate

    private fun setup(){

        supportActionBar?.hide()

        val txtRegister: TextView = findViewById(R.id.txtRegister)
        val txtEmail: TextView = findViewById(R.id.txtEmailSignup)
        val btnBuscar: Button = findViewById(R.id.btnRegister)

        txtRegister.setOnClickListener(View.OnClickListener {

            startActivity(Intent(this, Signup::class.java))

        })//setOnClick txtRegister

        btnBuscar.setOnClickListener {

            if(txtEmail.text.isNotEmpty()){

                restorePassword(txtEmail.text.toString())

            }//verificamos que el cuadro de email no este vacio

            else{

                showAlert("Debe llenar el cuadro de texto de correo")

            }//El cuadro de texto de email esta vacio

        }//setOnClickListener

    }//fun setup

    private fun restorePassword(email: String){

        val mAuth = FirebaseAuth.getInstance()

        mAuth.setLanguageCode("es")
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener {

            if(it.isSuccessful){

                showAlert("Enviando correo para restablecer tu contraseña...")
                startActivity(Intent(this, Login::class.java))

            }//correo enviado con éxito

            else{

                showAlert("No se ha podido enviar el correo para restablecer la contraseña")

            }//else

        }//addOnCompleteListener


    }//restore password

    private fun showAlert( message:String){

        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast.show()

    }//función show alert

}//class Forgot Password