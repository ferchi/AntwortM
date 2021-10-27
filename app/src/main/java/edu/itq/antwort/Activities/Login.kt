package edu.itq.antwort.Activities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import edu.itq.antwort.Classes.FirebaseService
import edu.itq.antwort.R

class Login : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FirebaseService.sharedPrefs = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->

            if (!task.isSuccessful) {
                Log.w(ContentValues.TAG, "Obtención fallida del token", task.exception)
            }//if isSuccessful

            val token = task.result
            FirebaseService.token = token
            // Setup
            setup()
            session()
            login(token ?: "")

        }//obtenemos el token nuevo del usuario

    }//fun onCreate

    @SuppressLint("ResourceType")
    private fun session() {

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email= prefs.getString("email", null)

        if(email != null){

            showHome(email)

        }//email

    }//session

    private fun setup(){

        supportActionBar?.hide()

        val txtRegister:TextView = findViewById(R.id.txtRegister)
        val txtForgetPassword:TextView = findViewById(R.id.txtFotgetPassword)

        txtForgetPassword.setOnClickListener {

            startActivity(Intent(this, ForgotPassword::class.java))

        }//setOnClick txtForgetPassword

        txtRegister.setOnClickListener {

            startActivity(Intent(this, Signup::class.java))

        }//setOnCLick txtRegister

    }//fun Setup

    private fun login(token: String){

        title = "Authentication"

        // Declaración de constantes

        val btnLogin: Button = findViewById(R.id.btnRegister)
        val txtEmail: TextView = findViewById(R.id.txtNameSignup)
        val txtPassword: TextView = findViewById(R.id.txtEmailSignup)

        btnLogin.setOnClickListener{

            if(txtEmail.text.isNotEmpty() && txtPassword.text.isNotEmpty()){

                FirebaseAuth.getInstance().signInWithEmailAndPassword(txtEmail.text.toString(), txtPassword.text.toString()).addOnCompleteListener { it ->

                    if(it.isSuccessful){

                        db.collection("Users").document(txtEmail.text.toString()).get().addOnSuccessListener {

                            updateToken(it.get("email") as String?, it.get("name") as String?, token)

                        }//add on success

                        showHome(it.result?.user?.email ?:"")

                    }//registro exitoso

                    else{

                        showAlert("Correo o contraseña incorrectos")

                    }//hubo un error en el registro del usuario

                }//add on complete listener

            }//if el campo de correo no está vacío

            else{

                if(txtEmail.text.isEmpty()){
                    
                    showAlert("No olvide rellenar el campo de correo electrónico")
                    
                }//el campo de email está vacío
                
                else{

                    showAlert("No olvide rellenar el campo de contraseña")

                }//el campo de cotraseña está vacío

            }//else

        }//setOnClick btnLogin

    }//fun Login

    private fun showHome(email: String){

        val homeIntent = Intent(this, HomeActivity::class.java).apply {

            putExtra("email", email)

        }//home intent

        startActivity(homeIntent)

    }//función showHome

    private fun showAlert( message:String){

        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast.show()

    }//función show alert

    private fun updateToken(email: String?, name: String?, token: String){

        db.collection("Users").document(email?:"").set(

            hashMapOf(

                "email" to email,
                "name" to name,
                "rol" to "",
                "token" to token

            )//hashMap

        )//set

    }//update token

}//class Login