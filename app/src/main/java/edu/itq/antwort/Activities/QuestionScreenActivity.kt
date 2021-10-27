package edu.itq.antwort.Activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import edu.itq.antwort.Methods
import edu.itq.antwort.databinding.ActivityNewQuestionBinding

class QuestionScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewQuestionBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        
        binding = ActivityNewQuestionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bundle = intent.extras
        val email = bundle?.getString("email")

        supportActionBar?.hide()

        // setup
        setup()

        db.collection("Users").document(email?:"").get().addOnSuccessListener {

            postQuestion(email, it.get("name") as String)

        }//obtenemos el nombre del usuario

    }//on onCreate

    private fun hideKeyboard(){

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)

    }//hideKeyboard

    private fun setup() {

        binding.edtTitle.requestFocus()

        binding.imgQuestionBack.setOnClickListener{

            hideKeyboard()
            onBackPressed()

        }//regresar a la pantalla anterior

    }//fun

    private fun postQuestion(email: String?, name : String){

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
                        "author" to email,
                        "title" to binding.edtTitle.text.toString(),
                        "description" to binding.edtDescription.text.toString()

                    )//hashMap

                )//set
                db.collection("Users").document(Methods.getEmail(this)!!).update("questions", FieldValue.increment(1))

                hideKeyboard()
                onBackPressed()

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

}//class

