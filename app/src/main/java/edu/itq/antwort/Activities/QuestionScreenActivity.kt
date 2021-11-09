package edu.itq.antwort.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import edu.itq.antwort.Methods
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ActivityNewQuestionBinding
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import edu.itq.antwort.Classes.NotificationData
import edu.itq.antwort.Classes.PushNotification
import edu.itq.antwort.Classes.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class QuestionScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewQuestionBinding
    private val db = FirebaseFirestore.getInstance()
    private var topicsList : MutableList<CharSequence> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        
        binding = ActivityNewQuestionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bundle = intent.extras
        val email = bundle?.getString("email")

        supportActionBar?.hide()

        // setup
        setup(email!!)

        db.collection("Users").document(email).get().addOnSuccessListener {

            postQuestion(email, it.get("name") as String, it.get("topics") as MutableList<CharSequence>)

        }//obtenemos el nombre del usuario

        binding.etNewQuestionTopics.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int ) {

            }
            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if(s.endsWith(' ') && (s.isNotBlank())){
                    addTag(s)
                    binding.etNewQuestionTopics.text.clear()
                }
            }
        })


        binding.etNewQuestionTopics.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                // if the event is a key down event on the enter button
                if (event.action == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    addTag(binding.etNewQuestionTopics.text)
                    binding.etNewQuestionTopics.text.clear()
                    return true
                }
                return false
            }
        })

    }//on onCreate

    @SuppressLint("InflateParams")
    private fun addTag(s: CharSequence) {
        val tagText = s.toString().lowercase().filter { !it.isWhitespace() }
        val layoutInflater = LayoutInflater.from(baseContext)
        val tag = layoutInflater.inflate(R.layout.item_topic_create, null, false) as Chip
        tag.text = tagText
        tag.setOnCloseIconClickListener {
            val text = (it as Chip).text
            topicsList.remove(text)
            binding.chipGroupNewQuestion.removeView(it)
        }

        if(tagText !in topicsList){
            topicsList.add(tagText)
            binding.chipGroupNewQuestion.addView(tag)
        }
    }

    private fun hideKeyboard(){

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)

    }//hideKeyboard


    private fun setup(email: String) {
        binding.edtTitle.requestFocus()
        loadImg(binding.imgQuestionProfile, email)
        binding.imgQuestionBack.setOnClickListener{

            hideKeyboard()
            onBackPressed()

        }//regresar a la pantalla anterior

    }//fun

    private fun loadImg(image : CircleImageView, author: String) {

        db.collection("Users").document(author).addSnapshotListener{
                result, error ->
            val urlImg = result!!.get("imgProfile").toString()

            try {
                if(urlImg.isNotEmpty())
                    Picasso.get().load(urlImg).into(image)

            } catch (e: Exception) {
                Picasso.get().load(R.drawable.ic_user_profile).into(image)
            }
        }
    }//load image

    private fun postQuestion(email: String?, name : String, userTopics: MutableList<CharSequence>){

        binding.btnPostQuestion.setOnClickListener {

            if(binding.edtTitle.text.toString().isNotEmpty() && binding.edtDescription.text.toString().isNotEmpty()){

                val id: String = db.collection("Questions").document().id
                val timestamp: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
                val likes: ArrayList<String> = ArrayList()
                val dislikes: ArrayList<String> = ArrayList()

                db.collection("Questions").document(id).set(

                    hashMapOf(

                        "id" to id,
                        "name" to name,
                        "date" to timestamp,
                        "likes" to likes,
                        "dislikes" to dislikes,
                        "answers" to 0,
                        "author" to email,
                        "title" to binding.edtTitle.text.toString(),
                        "description" to binding.edtDescription.text.toString(),
                        "topics" to topicsList

                    )//hashMap

                )//set

                userTopics.forEach {

                    Firebase.messaging.unsubscribeFromTopic("/topics/$it")

                }//forEach desuscribimos de los topics

                //showNotification("Nueva pregunta", "Nueva pregunta de: ", id, email!!, topicsList)
                db.collection("Users").document(Methods.getEmail(this)!!).update("questions", FieldValue.increment(1))

                hideKeyboard()
                val intent = Intent(this, HomeActivity::class.java).apply {

                    putExtra("email", email)

                }//intent

                startActivity(intent)

                userTopics.forEach {

                    Firebase.messaging.subscribeToTopic("/topics/$it")

                }//forEach desuscribimos de los topics

            }//los campos requeridos no estan vacios

            else{

                val toast = Toast.makeText(applicationContext, "No dejes ningún campo vacío", Toast.LENGTH_SHORT)
                toast.show()

                if(binding.edtTitle.text.toString().isEmpty()){

                    binding.edtTitle.requestFocus()

                }//el titulo esta vacío

                else {

                    binding.edtDescription.requestFocus()

                }//la descripción esta vacia

            }//los campos requeridos estan vacios

        }//setOnClickListener

    }//postQuestion

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

    private fun showNotification(title: String, message: String, question: String, email:String, topics: MutableList<CharSequence>){

            if(title.isNotEmpty() && message.isNotEmpty() && topics.isNotEmpty()) {

                topics.forEach {item->

                    PushNotification(

                        NotificationData(title, message, question, email),
                        "/topics/$item"

                    ).also {

                        sendNotification(it)

                    }//also

                }//forEach

            }//if

    }//show Notification

}//class
