package edu.itq.antwort.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ActivityEditQuestionBinding
import edu.itq.antwort.databinding.ActivityQuestionViewBinding

class EditQuestionActivity : AppCompatActivity() {

    lateinit var binding: ActivityEditQuestionBinding
    private val db = FirebaseFirestore.getInstance()
    private var topicsList : MutableList<CharSequence> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityEditQuestionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bundle = intent.extras
        val id = bundle?.getString("id")

        db.collection("Questions").document(id!!).get().addOnSuccessListener {

            setup(id, it.get("title") as String, it.get("description") as String, it.get("author") as String, it.get("topics") as ArrayList<String>)

        }//obtenemos los datos de la pregunta

    }//onCreate

    private fun setup(id: String, title: String, description: String, email: String, topics:ArrayList<String>){

        binding.imgQuestionBackEQ.setOnClickListener {

            onBackPressed()

        }//setOnClickListener

        binding.edtTitleEQ.requestFocus()
        loadImg(binding.imgQuestionProfileEQ, email)
        binding.edtTitleEQ.setText(title)
        binding.edtDescriptionEQ.setText(description)

        binding.btnUpdateQuestion.setOnClickListener {
            updateQuestion(id, binding.edtTitleEQ.text.toString(), binding.edtDescriptionEQ.text.toString(), email, topicsList as ArrayList<String>)

        }//setOnClickListener


        binding.etEditQuestionTopics.addTextChangedListener(object : TextWatcher {
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
                    binding.etEditQuestionTopics.text.clear()
                }
            }
        })


        binding.etEditQuestionTopics.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                // if the event is a key down event on the enter button
                if (event.action == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    addTag(binding.etEditQuestionTopics.text)
                    binding.etEditQuestionTopics.text.clear()
                    return true
                }
                return false
            }
        })

        if ((binding.chipGroupEditQuestion.childCount) == 0){
            topics.forEach {
                addTag(it)
            }
        }


    }//setup

    private fun addTag(s: CharSequence) {
        val tagText = s.toString().lowercase().filter { !it.isWhitespace() }
        val layoutInflater = LayoutInflater.from(baseContext)
        val tag = layoutInflater.inflate(R.layout.item_topic_create, null, false) as Chip
        tag.text = tagText

        if(tagText !in topicsList){
            topicsList.add(tagText)
            binding.chipGroupEditQuestion.addView(tag)
        }

        tag.setOnCloseIconClickListener {
            val text = (it as Chip).text
            topicsList.remove(text)
            binding.chipGroupEditQuestion.removeView(it)
        }
    }

    private fun updateQuestion(id: String, title: String, description: String, email: String, topics: ArrayList<String>) {

        if(title.isNotEmpty() && description.isNotEmpty()){

            db.collection("Questions").document(id).update("title", title)
            db.collection("Questions").document(id).update("description", description)
            db.collection("Questions").document(id).update("topics", topics)
            db.collection("Questions").document(id).update("edited",true)

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