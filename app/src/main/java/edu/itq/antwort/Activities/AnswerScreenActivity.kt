package edu.itq.antwort.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import edu.itq.antwort.Classes.NotificationData
import edu.itq.antwort.Classes.PushNotification
import edu.itq.antwort.Classes.RetrofitInstance
import edu.itq.antwort.Methods
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ActivityAnswerScreenBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnswerScreenActivity : AppCompatActivity() {

    private val TAG = "AnswerScreenActivity"

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

        loadImg(binding.imgUserAS, email!!)

        db.collection("Users").document(email).get().addOnSuccessListener {

            setup(it.get("name") as String)
            postAnswer(it.get("name") as String, it.get("rol") as String, email, question?:"", author?:"")

        }//obtenemos el nombre del usuario

    }//onCreate

    private fun loadImg(image : CircleImageView, author: String) {

        db.collection("Users").document(author).addSnapshotListener{
                result, _ ->
            val urlImg = result!!.get("imgProfile").toString()

            try {
                if(urlImg.isNotEmpty())
                    Picasso.get().load(urlImg).into(image)

            } catch (e: Exception) {
                Picasso.get().load(R.drawable.ic_user_profile).into(image)
            }
        }
    }//load image

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {

        try {

            val response = RetrofitInstance.api.postNotification(notification)

            if(response.isSuccessful){

                Log.d(TAG, "Response : ${Gson().toJson(response)}")

            }//if isSuccessful

            else{

                response.errorBody()?.let { Log.e(TAG, it.toString()) }

            }//else

        }catch (e: Exception) {

            Log.e(TAG, e.toString())

        }//try-catch

    }//sendNotifiaction

    private fun showNotification(title: String, message: String, question: String, email:String){

        db.collection("Users").document(email).get().addOnSuccessListener { it ->

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

    private fun setup(name:String){

        binding.txtAnswerAS.requestFocus()
        binding.txtNameAS.text = name
        binding.txtAnswerAS.hint = "Agrega tu respuesta"

        binding.imgBackAS.setOnClickListener {

            onBackPressed()

        }//setOnClickListener

    }//setup

    private fun postAnswer(name: String, rol: String , email: String, question: String, author: String){

        binding.btnPostAnswer.setOnClickListener {

            if(binding.txtAnswerAS.text.isNotEmpty()){

                val id: String = db.collection("Answers").document().id
                val timestamp: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
                val likes: ArrayList<String> = ArrayList()
                val dislikes: ArrayList<String> = ArrayList()

                db.collection("Answers").document(id).set(

                    hashMapOf(

                        "id" to id,
                        "nameAuthor" to name,
                        "date" to timestamp,
                        "author" to email,
                        "likes" to likes,
                        "dislikes" to dislikes,
                        "verified" to (rol == "Facilitador"),
                        "content" to binding.txtAnswerAS.text.toString(),
                        "question" to question

                    )//hashMap

                )//set

                db.collection("Users").document(Methods.getEmail(this)!!).update("answers", FieldValue.increment(1))
                db.collection("Questions").document(question).update("answers", FieldValue.increment(1))

                if(author != email){

                    db.collection("Questions").document(question).get().addOnSuccessListener {

                        createNotification("$name ha respondido tu pregunta:", it.get("title") as String, email, question, author, timestamp)
                        showNotification("$name ha respondido tu pregunta:", it.get("title") as String,question,  author)

                    }//addOnSuccessListener

                }//if solo creamos notificacion si nosostros no interactuamos con nuestro post

                onBackPressed()

            }//el campo de respuesta no esta vacio

            else{

                Toast.makeText(applicationContext, "No dejes campos vacios", Toast.LENGTH_SHORT).show()
                binding.txtAnswerAS.requestFocus()

            }//el campo de respuesta esta vacio

        }//setOnClickListener btnPostAnswer

    }//postAnswer

    private fun createNotification(title: String, content: String, user:String, question: String, author: String, timestamp: com.google.firebase.Timestamp) {

        val id: String = db.collection("Notifications").document().id

        db.collection("Notifications").document(id).set(

            hashMapOf(

                "id" to id,
                "title" to title,
                "author" to user,
                "content" to content,
                "question" to question,
                "user" to author,
                "date" to timestamp

            )//hashMapOf con los nuevos datos

        )//creamos la notificacion

    }//createNotification

}//class