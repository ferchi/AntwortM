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


            binding.textCareer.text = (it.get("specialty") as String)


            binding.textRol.text = (it.get("rol") as String)


            binding.showCareer.setOnClickListener {

                if (binding.textCareer.visibility == View.GONE) {

                    binding.textCareer.visibility = View.VISIBLE
                    binding.showCareer.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_expand_less_24,
                        0
                    )

                }//if

                else {

                    binding.textCareer.visibility = View.GONE
                    binding.showCareer.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_expand_more_24,
                        0
                    )

                }//else

            }//setOnClickListener myQuestions

            binding.showRol.setOnClickListener {

                if (binding.textRol.visibility == View.GONE) {

                    binding.textRol.visibility = View.VISIBLE
                    binding.showRol.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_expand_less_24,
                        0
                    )

                }//if

                else {

                    binding.textRol.visibility = View.GONE
                    binding.showRol.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_expand_more_24,
                        0
                    )

                }//else

            }//setOnClickListener myQuestions

            binding.txtTotalQuestions.setOnClickListener {

                if (binding.questionsConstraintLayout.visibility == View.GONE) {

                    binding.questionsConstraintLayout.visibility = View.VISIBLE
                    binding.txtTotalQuestions.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_expand_less_24,
                        0
                    )

                }//if

                else {

                    binding.questionsConstraintLayout.visibility = View.GONE
                    binding.txtTotalQuestions.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_expand_more_24,
                        0
                    )

                }//else

            }//setOnClickListener myQuestions

            binding.txtOtherQuestions.setOnClickListener {

                if (binding.constraintAnswers.visibility == View.GONE) {

                    binding.constraintAnswers.visibility = View.VISIBLE
                    binding.txtOtherQuestions.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_expand_less_24,
                        0
                    )

                }//if

                else {

                    binding.constraintAnswers.visibility = View.GONE
                    binding.txtOtherQuestions.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_expand_more_24,
                        0
                    )

                }//else

            }//setOnClickListener OtherQuestions

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

        binding.textAsked.text = "Realizadas: $questions"
        binding.textLikesReceived.text = "Tus preguntas han recibido $likes \"útil\""
        binding.textDislikesReceived.text = "Tus preguntas han recibido $dislikes \"poco útil\""
        binding.textLikesPercent.text = "Un $percentLikes% de los usuarios les parecen útiles tus preguntas"
        binding.textDislikesPercent.text = "Un $percentDislikes% de los usuarios les parecen poco útiles tus preguntas"

    }//fill myQuestions

    private fun fillOtherQuestions(likes: Int, dislikes: Int, answers: Long) {

        val totalVotes = if(likes + dislikes == 0) 1 else likes + dislikes

        val percentLikes = (likes * 100) / totalVotes
        val percentDislikes =  (dislikes * 100) / totalVotes

        binding.textWrittenResponses.text = "Has escrito $answers respuestas"
        binding.textLikesReceivedAnswers.text = "Tus respuestas han recibido $likes \"útil\""
        binding.textDislikesReceivedAnswers.text = "Tus respuestas han recibido $dislikes \"poco útil\""
        binding.textLikesPercentAnswers.text = "Un $percentLikes% de los usuarios les parecen útiles tus respuestas"
        binding.textDislikesPercentAnswers.text = "Un $percentDislikes% de los usuarios les parecen poco útiles tus respuestas"

    }//fillOtherQuestions

}//class