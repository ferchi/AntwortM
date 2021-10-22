package edu.itq.antwort.Activitys

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import edu.itq.antwort.Classes.NotificationData
import edu.itq.antwort.Classes.PushNotification
import edu.itq.antwort.Classes.RetrofitInstance
import edu.itq.antwort.databinding.ActivityAnswerScreenBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnswerScreenActivity : AppCompatActivity() {

    val TAG = "AnswerScreenActivity"

    lateinit var binding: ActivityAnswerScreenBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityAnswerScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val bundle = intent.extras
        val email = bundle?.getString("email")
        val question = bundle?.getString("question")
        val author = bundle?.getString("author")


        db.collection("Users").document(email?:"").get().addOnSuccessListener {

            setup(it.get("name") as String?:"", email?:"", author?:"")
            postAnswer(it.get("name") as String?:"", email?:"", question?:"", author?:"")

        }//obtenemos el nombre del usuario

    }//onCreate

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {

        try {

            val response = RetrofitInstance.api.postNotification(notification)

            if(response.isSuccessful){

                Log.d(TAG, "Response : ${Gson().toJson(response)}")

            }//if isSuccessful

            else{

                response.errorBody()?.let { Log.e(TAG, it.string()) }

            }//else

        }catch (e: Exception) {

            Log.e(TAG, e.toString())

        }//try-catch

    }//sendNotifiaction

    private fun showNotification(title: String, message: String, question: String, email:String){

        db.collection("Users").document(email).get().addOnSuccessListener {

            val recipientToken = it.get("token") as String?

            if(title.isNotEmpty() && message.isNotEmpty()){

                PushNotification(

                    NotificationData(title, message, question, email),
                    recipientToken?:""

                ).also {

                    sendNotification(it)

                }//also

            }//obtener token del usuario

        }//obtenemos los datos de quien dio like

    }//show Notification

    private fun setup(name:String, email: String, author: String){

        binding.txtAnswerAS.requestFocus()
        binding.txtNameAS.text = name
        binding.txtAnswerAS.hint = "Agrega tu respuesta"
        binding.imgBackAS.setOnClickListener {

            onBackPressed()
            hideKeyboard()

        }//setOnClickListener

    }//setup

    private fun postAnswer(name: String , email: String, question: String, author: String){

        hideKeyboard()

        binding.btnPostAnswer.setOnClickListener {

            if(binding.txtAnswerAS.text.isNotEmpty()){

                val id: String = db.collection("Answers").document().id
                val timestamp = com.google.firebase.firestore.FieldValue.serverTimestamp()

                db.collection("Answers").document(id).set(

                    hashMapOf(

                        "id" to id,
                        "nameAuthor" to name,
                        "date" to timestamp,
                        "author" to email,
                        "content" to binding.txtAnswerAS.text.toString(),
                        "question" to question

                    )//hashMap

                )//set

                if(author != email){

                    db.collection("Questions").document(question).get().addOnSuccessListener {

                        createNotification("$name ha respondido tu pregunta:", it.get("title") as String?:"", email, question, author)
                        showNotification("$name ha respondido tu pregunta:", it.get("title") as String?:"",question,  author)

                    }//addOnSuccessListener

                }//if solo creamos notificacion si nosostros no interactuamos con nuestro post

                hideKeyboard()
                onBackPressed()

            }//el campo de respuesta no esta vacio

            else{

                showAlert("No deje campos vacios")
                binding.txtAnswerAS.requestFocus()

            }//el campo de respuesta esta vacio

        }//setOnClickListener btnPostAnswer

    }//postAnswer


    private fun hideKeyboard(){

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)

    }//hideKeyboard

    private fun showAlert( message:String){

        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast.show()

    }//función show alert

    private fun createNotification(title: String, content: String, user:String, question: String, author: String) {

        val id: String = db.collection("Notifications").document().id

        db.collection("Notifications").document(id).set(

            hashMapOf(

                "id" to id,
                "title" to title,
                "author" to user,
                "content" to content,
                "question" to question,
                "user" to author

            )//hashMapOf con los nuevos datos

        )//creamos la notificacion

    }//createNotification

}//class