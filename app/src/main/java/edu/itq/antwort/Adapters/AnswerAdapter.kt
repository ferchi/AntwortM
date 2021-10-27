package edu.itq.antwort.Adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import edu.itq.antwort.Activities.ProfileActivity
import edu.itq.antwort.Activities.QuestionDetails
import edu.itq.antwort.Classes.*
import edu.itq.antwort.Fragments.TAG
import edu.itq.antwort.R
import edu.itq.antwort.Methods
import edu.itq.antwort.databinding.ItemAnswerViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnswerAdapter (private val fragment: Fragment, private val dataset: List<Answers>):
    RecyclerView.Adapter<AnswerAdapter.ViewHolder>() {

    class ViewHolder (val binding: ItemAnswerViewBinding) : RecyclerView.ViewHolder(binding.root)

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemAnswerViewBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount() = dataset.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user = Methods.getEmail(fragment.requireActivity())

        val answer = dataset[position]

        if(answer.verified){
            holder.binding.imgVerifiedUser.visibility = View.VISIBLE
        }

        holder.binding.imgUserAV.setOnClickListener {

            val intent = Intent(fragment.requireContext(), ProfileActivity::class.java).apply {

                putExtra("author", answer.author)

            }//intent

            fragment.startActivity(intent)
        }

        reactions(answer,null, answer.likes, answer.dislikes, holder.binding.likesIA, "Útil", user!!, answer.author, answer.id, answer.question, "Han reaccionado a tu respuesta", "Answers", "content", position)
        reactions(answer,null, answer.dislikes, answer.likes, holder.binding.dislikeIA, "No útil", user, answer.author, answer.id, answer.question, "Han reaccionado a tu respuesta", "Answers", "content", position)

        holder.binding.txtNameAV.text = answer.nameAuthor
        holder.binding.txtAnswersAV.text = answer.content
        holder.binding.cardItemAnswer.setOnClickListener {

            val homeIntent = Intent(fragment.requireContext(), QuestionDetails::class.java).apply {

                putExtra("id", answer.question)

            }//homeIntent

            fragment.startActivity(homeIntent)

        }
    }

    private fun reactions(modelAnswers: Answers?, modelQuestions: Questions?, mainArray: ArrayList<String>, secondArray: ArrayList<String>, txtReaction: TextView, text: String, user: String, author: String, id: String, question: String, title: String, collection: String, content: String, position: Int){

        if(mainArray.isNotEmpty()){

            txtReaction.text = mainArray.size.toString()

            if(mainArray.contains(user) && !secondArray.contains(user)){

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

            if(mainArray.contains(user)){

                mainArray.remove(user)

            }//si mi correo esta en la lista

            else{

                if(secondArray.contains(user)){

                    secondArray.remove(user)

                }// si estoy es la segunda lista eliminamos mi correo

                if(user != author){

                    db.collection(collection).document(id).get().addOnSuccessListener {

                        createNotification(title, it.get(content) as String, user, question, author)
                        showNotification(title, it.get(content) as String, question, author)

                    }//obtenemos el contenido de la respuesta

                }//creamos notificacion solo si no estoy dandome like a mi mismo

                mainArray.add(user)

            }//mi correo no esta en la lista

            if(modelAnswers != null){

                updateAnswers(modelAnswers,position)

            }//el modelo de respuestas no es nulo

            if(modelQuestions != null){

                updateQuestions(modelQuestions)

            }//el modelo de preguntas no es nulo

        }//se le dio click al boton

    }//reactions


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

    private fun updateAnswers(model: Answers, position: Int){
        db.collection("Answers").document(model.id).set(

            hashMapOf(

                "id" to model.id,
                "nameAuthor" to model.nameAuthor,
                "author" to model.author,
                "date" to model.date,
                "verified" to model.verified,
                "content" to model.content,
                "question" to model.question,
                "likes" to model.likes,
                "dislikes" to model.dislikes

            )//hashMapOf con los nuevos datos

        )//actualizamos el numero de likes
        this.notifyItemChanged(position)
    }//update answers

    private fun updateQuestions(model: Questions){

        db.collection("Questions").document(model.id).set(

            hashMapOf(

                "id" to model.id,
                "name" to model.name,
                "author" to model.author,
                "date" to model.date,
                "title" to model.title,
                "description" to model.description,
                "likes" to model.likes,
                "dislikes" to model.dislikes

            )//hashMapOf con los nuevos datos

        )//actualizamos el numero de likes

    }//update questions
}