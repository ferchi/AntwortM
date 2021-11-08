package edu.itq.antwort.Activities

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import com.google.firebase.auth.FirebaseUser
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

            db.collection("Users").document(email).get().addOnSuccessListener {

                val updated = it.get("updated") as Boolean
                val verified = it.get("rol") as String == "facilitador"

                if(!updated && verified){

                    updateAnswersVerified(email)
                    db.collection("Users").document(email).update("updated", true)

                }//actualizamos las respuestas anteriores dadas por el usuario

            }//obtenemos el campo updated

            showHome(email)

        }//email

    }//session

    private fun updateToken(email: String?, token: String){

        db.collection("Users").document(email?:"").update("token", token)

    }//update token

    private fun updateAnswersVerified(email:String){

        db.collection("Answers").whereEqualTo("author", email).get().addOnSuccessListener {

            it.documents.forEach { i->

                val id = i.get("id") as String

                db.collection("Answers").document(id).update("verified", true)

            }//for each

        }//obtenemos el id de las perguntas hechas por el usuario

    }//verified

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

    override fun onBackPressed() {
        this.moveTaskToBack(true);
    }//onBackPressed()

    private fun login(token: String){

        title = "Authentication"

        // Declaración de constantes

        val btnLogin: Button = findViewById(R.id.btnRegister)
        val txtEmail: TextView = findViewById(R.id.txtNameSignup)
        val txtPassword: TextView = findViewById(R.id.txtEmailSignup)

        btnLogin.setOnClickListener{

            if(txtEmail.text.isNotEmpty() && txtPassword.text.isNotEmpty()){

                val mAuth = FirebaseAuth.getInstance()
                mAuth.setLanguageCode("es")

                mAuth.signInWithEmailAndPassword(txtEmail.text.toString(), txtPassword.text.toString()).addOnCompleteListener {

                    if(it.isSuccessful){

                        updateToken(txtEmail.text.toString(), token)
                        showHome(it.result?.user?.email ?:"")

/*
                        val user : FirebaseUser = mAuth.currentUser!!

                        if(user.isEmailVerified){

                            updateToken(txtEmail.text.toString(), token)
                            showHome(it.result?.user?.email ?:"")

                        }//el usuario esta verificado

                        else{

                            showAlert()

                        }//else el usuario no ha validado su email
*/
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

    private fun showAlert(){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Parece que su cuenta no ha sido verificada aun")
        builder.setMessage("¿Desea reenviar el correo de verificación?")

        builder.setPositiveButton("Sí"
        ) { _, _ ->

            val mAuth = FirebaseAuth.getInstance()
            mAuth.setLanguageCode("es")

            val user : FirebaseUser = mAuth.currentUser!!
            user.sendEmailVerification().addOnSuccessListener {

                Toast.makeText(this, "Hemos reenviado el correo de verificación a su correo", Toast.LENGTH_SHORT).show()

            }.addOnFailureListener{

                Log.d(ContentValues.TAG, "Correo no enviado ${it.message}")

            }//fallo al enviar correo

        }//boton sí

        builder.setNegativeButton("No", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()

    }//show alert

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

}//class Login