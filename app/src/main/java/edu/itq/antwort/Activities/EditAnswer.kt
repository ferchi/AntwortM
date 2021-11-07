package edu.itq.antwort.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ActivityEditAnswerBinding

class EditAnswer : AppCompatActivity() {

    lateinit var binding: ActivityEditAnswerBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityEditAnswerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bundle = intent.extras
        val email = bundle?.getString("email")
        val id = bundle?.getString("id")

        db.collection("Answers").document(id!!).get().addOnSuccessListener {

            setup(email!!, it.get("content") as String, id, it.get("nameAuthor") as String)

        }//obtenemos el contenido de la respuesta

    }//onCreate

    private fun setup(email: String, text: String, id: String, name: String) {

        loadImg(binding.imgUserEA, email)
        binding.txtNameEA.text = name

        binding.imgBackEA.setOnClickListener{

            onBackPressed()

        }//setOnClickListener

        binding.txtAnswerEA.setText(text)
        binding.txtAnswerEA.requestFocus()

        binding.btnEditAnswer.setOnClickListener{

            if(binding.txtAnswerEA.text.isNotEmpty()){

                db.collection("Answers").document(id).update("content", binding.txtAnswerEA.text.toString())
                Toast.makeText(this, "Respuesta actualizada con éxito", Toast.LENGTH_SHORT).show()
                onBackPressed()

            }//la nueva respuesta no esta vacia

            else{

                Toast.makeText(this, "No deje el campo de respuesta vacio", Toast.LENGTH_SHORT).show()

            }//la nueva respuesta esta vacia

        }//setOnClickListener

    }//setup

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

    }//loadImage

}//class