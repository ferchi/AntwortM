package edu.itq.antwort.Adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import edu.itq.antwort.Activities.AnswerScreenActivity
import edu.itq.antwort.Activities.ProfileActivity
import edu.itq.antwort.Activities.QuestionDetails
import edu.itq.antwort.Classes.NotificationData
import edu.itq.antwort.Classes.PushNotification
import edu.itq.antwort.Classes.Questions
import edu.itq.antwort.Classes.RetrofitInstance
import edu.itq.antwort.Fragments.TAG
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ItemQuestionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuestionAdapter (private val fragment: Fragment, private val dataset: List<Questions>):
    RecyclerView.Adapter<QuestionAdapter.ViewHolder>() {

    class ViewHolder (val binding: ItemQuestionBinding) : RecyclerView.ViewHolder(binding.root)

    private val db = FirebaseFirestore.getInstance()
    private var positionVar : Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemQuestionBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount() = dataset.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val question = dataset[position]

        holder.binding.answersIQ.text = question.answers.toString()
        holder.binding.txtTitleText.text = question.title
        holder.binding.txtItemDescription.text = question.description
        holder.binding.txtAuthor.text = question.name

        reactions(question, question.likes, question.dislikes, holder.binding.likesIA, "Útil", question.author, question.title, question.id, position)
        reactions(question, question.dislikes, question.likes, holder.binding.dislikesIQ, "No útil", question.author, question.title, question.id, position)


        holder.binding.answersIQ.setOnClickListener {
            val answerIntent = Intent(fragment.requireContext(), AnswerScreenActivity::class.java).apply {

                putExtra("email", getEmail())
                putExtra("author", question.author)
                putExtra("authorName", question.name)
                putExtra("question", question.id)

            }//answerIntent
            fragment.startActivity(answerIntent)
        }

        holder.binding.imgAuthorAI.setOnClickListener {
            val homeIntent = Intent(fragment.requireContext(), ProfileActivity::class.java).apply {

                putExtra("id", question.id)

            }//homeIntent

            fragment.startActivity(homeIntent)
        }

        holder.binding.cvSI.setOnClickListener {
            val homeIntent = Intent(fragment.requireContext(), QuestionDetails::class.java).apply {

                putExtra("id", question.id)

            }//homeIntent
            fragment.startActivity(homeIntent)
        }
    }

    private fun reactions(model: Questions, mainArray: ArrayList<String>, secondArray: ArrayList<String>, txtReaction: TextView, text: String, author: String, title: String, id:String, position: Int){

        if(mainArray.isNotEmpty()){

            txtReaction.text = mainArray.size.toString()

            if(mainArray.contains(getEmail()) && !secondArray.contains(getEmail())){

                reactionColor(txtReaction, true)

            }//Estoy en la lista de likes y no en la de dislikes

            else{

                reactionColor(txtReaction, false)

            }//no le he dado like al post

        }//los likes no estan vacios

        else{

            txtReaction.text = text
            reactionColor(txtReaction, false)

        }//no tiene likes

        txtReaction.setOnClickListener {

            if(mainArray.contains(getEmail())){

                mainArray.remove(getEmail().toString())

            }//si mi correo esta en la lista de dislikes

            else{

                if(secondArray.contains(getEmail())){

                    secondArray.remove(getEmail().toString())

                }//eliminiamos el correo de la lista de likes

                //Crear la notificación

                if(getEmail() != author){

                    createNotification(title, id, author)
                    showNotification(title, id, author)

                }//creamos notificacion solo si no estoy dandome like a mi mismo

                mainArray.add(getEmail().toString())

            }// mi correo no esta en la lista de likes

            updateQuestion(model, position)

        }//setOnClickListener

    }//reactions

    private fun updateQuestion(model: Questions, position: Int){

        db.collection("Questions").document(model.id).set(

            hashMapOf(

                "id" to model.id,
                "author" to model.author,
                "name" to model.name,
                "date" to model.date,
                "title" to model.title,
                "description" to model.description,
                "likes" to model.likes,
                "dislikes" to model.dislikes

            )//hashMapOf con los nuevos datos

        )//actualizamos el numero de likes

        this.notifyItemChanged(position )

    }//update question

    private fun reactionColor(txtReaction: TextView, like: Boolean){

        if(like){

            txtReaction.compoundDrawables[0].setTint(Color.parseColor("#FB771E"))
            txtReaction.setTextColor(Color.parseColor("#FB771E"))

        }// se le dio like

        else{

            txtReaction.compoundDrawables[0].setTint(-1979711488)
            txtReaction.setTextColor(-1979711488)

        }//else se le dio dislike

    }//reaction color

    private fun getEmail() : String?{

        val prefs : SharedPreferences =  fragment.requireActivity().getSharedPreferences(fragment.getString(R.string.prefs_file), Context.MODE_PRIVATE)
        return prefs.getString("email", null)

    }//getMail

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

    private fun showNotification(message: String, question: String, email:String){

        db.collection("Users").document(email).get().addOnSuccessListener {

            val recipientToken = it.get("token") as String?

            if(message.isNotEmpty()){

                PushNotification(

                    NotificationData("Han reaccionado a tu pregunta:", message, question, email),
                    recipientToken?:""

                ).also {

                    sendNotification(it)

                }//also

            }//obtener token del usuario

        }//obtenemos los datos de quien dio like

    }//show Notification

    private fun createNotification(content: String, question: String, author: String) {

        val id: String = db.collection("Notifications").document().id

        db.collection("Notifications").document(id).set(

            hashMapOf(

                "id" to id,
                "title" to "Han reaccionado a tu pregunta:",
                "author" to getEmail(),
                "content" to content,
                "question" to question,
                "user" to author

            )//hashMapOf con los nuevos datos

        )//creamos la notificacion

    }//createNotification


}