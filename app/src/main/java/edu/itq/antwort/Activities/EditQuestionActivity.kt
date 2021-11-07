package edu.itq.antwort.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ActivityEditQuestionBinding
import edu.itq.antwort.databinding.ActivityQuestionViewBinding

class EditQuestionActivity : AppCompatActivity() {

    lateinit var binding: ActivityEditQuestionBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityEditQuestionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bundle = intent.extras
        val id = bundle?.getString("id")

        db.collection("Questions").document(id!!).get().addOnSuccessListener {

            setup(id, it.get("title") as String, it.get("description") as String, it.get("author") as String)

        }//obtenemos los datos de la pregunta

    }//onCreate

    private fun setup(id: String, title: String, description: String, email: String){

        binding.imgQuestionBackEQ.setOnClickListener {

            onBackPressed()

        }//setOnClickListener

        binding.edtTitleEQ.requestFocus()
        loadImg(binding.imgQuestionProfileEQ, email)
        binding.edtTitleEQ.setText(title)
        binding.edtDescriptionEQ.setText(description)

        binding.btnUpdateQuestion.setOnClickListener {

            updateQuestion(id, binding.edtTitleEQ.text.toString(), binding.edtDescriptionEQ.text.toString(), email)

        }//setOnClickListener

    }//setup

    private fun updateQuestion(id: String, title: String, description: String, email: String) {

        if(title.isNotEmpty() && description.isNotEmpty()){

            db.collection("Questions").document(id).update("title", title)
            db.collection("Questions").document(id).update("description", description)
            Toast.makeText(this, "Publicación actualizada con éxito", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, HomeActivity::class.java).apply {

                putExtra("email", email)

            }//intent

            startActivity(intent)

        }//los campos nos están vacios

        else{

            Toast.makeText(this, "No deje campos en blanco", Toast.LENGTH_SHORT).show()

        }//else los campos están vacios

    }//updateQuestion

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

}//EditQuestionActivity