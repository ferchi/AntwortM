package edu.itq.antwort.Activities

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import edu.itq.antwort.Classes.FirebaseService
import edu.itq.antwort.databinding.ActivitySignupBinding

class Signup : AppCompatActivity() {

    lateinit var binding: ActivitySignupBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        FirebaseService.sharedPrefs = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Error al guardar el token de FCM", task.exception)
            }//if isSuccessful

            val token = task.result
            FirebaseService.token = token
            register(token ?: "")

        }//guardamos el token en la bd

        // Setup

        setup()

    }//fun onCreate

    private fun setup(){

        supportActionBar?.hide()

        binding.txtLogin.setOnClickListener{

            startActivity(Intent(this, Login::class.java))

        }//setOnClick txtLogin

    }//fun setup

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun register(recipientToken: String){

        title = "SignUp"

        binding.btnRegister.setOnClickListener {

            if(binding.txtNameSignup.text.isNotEmpty() && binding.txtEmailSignup.text.isNotEmpty() && binding.txtPasswordSignup.text.isNotEmpty() && binding.txtConfirmPasswordSignup.text.isNotEmpty() ){

                if(binding.txtPasswordSignup.text.length > 7){

                    if(binding.txtPasswordSignup.text.toString() == binding.txtConfirmPasswordSignup.text.toString()){

                        if(validateText(binding.txtEmailSignup.text.toString(), "[\\w-\\.]+\\@((queretaro.tecnm.mx))")){

                            val isStudent = validateText(binding.txtEmailSignup.text.toString(), "[lL][cC]?[0-9]{8}@(queretaro.tecnm.mx)")
                            val rol = if(isStudent) "estudiante" else "docente"
/*
                            if(isStudent)
                                Toast.makeText(this, "Correo de alumno", Toast.LENGTH_SHORT).show()
                            else
                                Toast.makeText(this, "Correo de docente", Toast.LENGTH_SHORT).show()
*/
                            val mAuth = FirebaseAuth.getInstance()

                            mAuth.createUserWithEmailAndPassword(binding.txtEmailSignup.text.toString(), binding.txtPasswordSignup.text.toString()).addOnCompleteListener {

                                if (it.isSuccessful) {

                                    val topics: ArrayList<String> = ArrayList()

                                    db.collection("Users").document(binding.txtEmailSignup.text.toString()).set(

                                        hashMapOf(

                                            "email" to binding.txtEmailSignup.text.toString(),
                                            "name" to binding.txtNameSignup.text.toString(),
                                            "rol" to rol,
                                            "specialty" to "",
                                            "token" to recipientToken,
                                            "updated" to false,
                                            "answers" to 0,
                                            "questions" to 0,
                                            "topics" to topics,
                                            "qlikes" to 0,
                                            "qdislikes" to 0,
                                            "alikes" to 0,
                                            "adislikes" to 0,
                                            "otherAnswered" to 0,
                                            "imgProfile" to "https://firebasestorage.googleapis.com/v0/b/antwort-872bd.appspot.com/o/profileImages%2Fic_action_profile.png?alt=media&token=0df24c61-cf48-4a74-bbe6-e1bee33361c2"

                                        )//hashMap

                                    )//set

                                    validateEmail()

                                }//if

                                else{

                                    showAlert("El correo ingresado ya tiene una cuenta asociada")

                                }//else

                            }//createUserWithEmailAndPassword

                        }//revisamos si el correo pertenece al dominio queretato.tecnm.mx o mail.itq.edu.mx

                        else{

                            showAlert("Favor de registrarse con el correo institucional")

                        }//correo que no es de alumno

                    }//las contraseñas coinciden

                    else{

                        showAlert("Las contraseñas no coinciden")

                    }//las constraseñas no coinciden

                }//if la contraseña cumple con los requisitos

                else{

                    showAlert("La contraseña debe tener minimo 8 caracteres")

                }//else requerimientos de la contraseña

            }//if los campos de texto no estan vacios

            else{

                showAlert("Favor de rellenar todos los campos")

            }//Algún campo está vacío

        }//setOnClickListener btnLogin

    }//función register

    override fun onBackPressed() {

        startActivity(Intent(this, Login::class.java))

    }//onBackPressed()

    private fun validateText(text: String, regex: String) : Boolean{

        val condition = regex.toRegex()
        val matchResult = condition.matchEntire(text)

        return matchResult != null

    }//función para validar correos institucionales

    private fun showAlert( message:String){

        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast.show()

    }//función show alert

    private fun validateEmail(){

        val mAuth = FirebaseAuth.getInstance()
        mAuth.setLanguageCode("es")

        val user : FirebaseUser = mAuth.currentUser!!
        user.sendEmailVerification().addOnSuccessListener {

            Toast.makeText(this, "Hemos enviado un correo de verificación a su correo", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))

        }.addOnFailureListener{

            Log.d(TAG, "Correo no enviado ${it.message}")

        }//fallo el envio

    } //validar Email

}//class signup

