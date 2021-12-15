package edu.itq.antwort.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ActivityAnalyticsBinding
import edu.itq.antwort.databinding.ActivityEditProfileBinding

class AnalyticsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    lateinit var binding: ActivityAnalyticsBinding
    lateinit var currentUser: String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bundle = intent.extras
        currentUser = bundle?.getString("user").toString()

        setup()

    }//onCreate

    private fun setup(){

        binding.imgBackAnalytics.setOnClickListener { onBackPressed() }

        db.collection("Users").document(currentUser!!).get().addOnSuccessListener {

            getReactionsReceivedOnQuestions(
                "Questions",
                it.get("questions") as Long,
                it.get("answers") as Long
            )
            getReactionsReceivedOnQuestions(
                "Answers",
                it.get("questions") as Long,
                it.get("answers") as Long
            )


            binding.tvCareer.text = (it.get("specialty") as String)
            binding.tvRole.text = (it.get("rol") as String)


        }//obtenemos los datos de la base de datos

    }//setup analytics

    private fun getReactionsReceivedOnQuestions(collection: String, question: Long, answer: Long){

        db.collection(collection).whereEqualTo("author", currentUser).get().addOnSuccessListener {

            var likes = 0
            var dislikes = 0

            it.documents.forEach { question ->

                likes += (question.get("likes") as ArrayList<*>).size
                dislikes += (question.get("dislikes") as ArrayList<*>).size

            }//forEach

            if(collection=="Questions")
                fillMyQuestions(likes, dislikes, question)
            else
                fillOtherQuestions(likes, dislikes, answer)

        }//obtenemos todas las preguntas del usuario

    }//getLikesReceived

    private fun fillMyQuestions(likes: Int, dislikes: Int, questions: Long){

        val totalVotes = if(likes + dislikes == 0) 1 else likes + dislikes

        val percentLikes = (likes * 100) / totalVotes
        val percentDislikes =  (dislikes * 100) / totalVotes

        binding.tvTotalQuestions.text = questions.toString()
        binding.tvQuestionUtil.text = likes.toString()
        binding.tvQuestionNoutils.text = dislikes.toString()
        binding.tvQuestionUtilPercent.text = "$percentLikes%"
        binding.tvQuestionNoutilPercent.text = "$percentDislikes%"

    }//fill myQuestions

    private fun fillOtherQuestions(likes: Int, dislikes: Int, answers: Long) {

        val totalVotes = if(likes + dislikes == 0) 1 else likes + dislikes

        val percentLikes = (likes * 100) / totalVotes
        val percentDislikes =  (dislikes * 100) / totalVotes

        binding.tvTotalAnswers.text = answers.toString()
        binding.tvAnswerUtil.text = likes.toString()
        binding.tvAnswerNoutil.text = dislikes.toString()
        binding.tvAnswerUtilPercent.text = "$percentLikes%"
        binding.tvAnswerNoutilPercent.text = "$percentDislikes%"

    }//fillOtherQuestions

}//class