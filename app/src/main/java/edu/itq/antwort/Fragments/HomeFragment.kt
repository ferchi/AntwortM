package edu.itq.antwort.Fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import edu.itq.antwort.Activitys.AnswerScreenActivity
import edu.itq.antwort.Activitys.ProfileActivity
import edu.itq.antwort.Activitys.QuestionDetails
import edu.itq.antwort.Activitys.SearchActivity
import edu.itq.antwort.Classes.NotificationData
import edu.itq.antwort.Classes.PushNotification
import edu.itq.antwort.Classes.Questions
import edu.itq.antwort.Classes.RetrofitInstance
import edu.itq.antwort.R
import edu.itq.antwort.databinding.FragmentHomeBinding
import edu.itq.antwort.databinding.ItemQuestionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuestionsViewHolder(val questionBinding: ItemQuestionBinding) : RecyclerView.ViewHolder(questionBinding.root)

val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var db : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_home, container, false)

    }//onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)
        db = FirebaseFirestore.getInstance()

        binding.includeToolbar.imgSearchTB.setOnClickListener {

            startActivity(Intent(context, SearchActivity::class.java))

        }//setOnClickListener

        showQuestions()

    }//onViewCreated

    private fun showQuestions(){

        val query = db.collection("Questions").orderBy("date", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<Questions>().setQuery(query, Questions::class.java).setLifecycleOwner(this).build()

        val adapter = object: FirestoreRecyclerAdapter<Questions, QuestionsViewHolder>(options){

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionsViewHolder {

                return QuestionsViewHolder(ItemQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

            }//onCreateViewHolder

            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(holder: QuestionsViewHolder, position: Int, model: Questions) {

                holder.questionBinding.imgAuthorAI.setOnClickListener {

                    val homeIntent = Intent(context, ProfileActivity::class.java).apply {

                        putExtra("id", model.id)

                    }//homeIntent

                    startActivity(homeIntent)

                }//setOnClickListener

                holder.questionBinding.cvSI.setOnClickListener {

                    val homeIntent = Intent(context, QuestionDetails::class.java).apply {

                        putExtra("id", model.id)

                    }//homeIntent

                    startActivity(homeIntent)

                }//setOnClickListener

                reactions(model, model.likes, model.dislikes, holder.questionBinding.likesIA, "Útil", model.author, model.title, model.id)
                reactions(model, model.dislikes, model.likes, holder.questionBinding.dislikesIQ, "No útil", model.author, model.title, model.id)

                //--------------------------------------------- Programación del boton responder --------------------------------------------- //

                holder.questionBinding.answersIQ.setOnClickListener {

                    val answerIntent = Intent(context, AnswerScreenActivity::class.java).apply {

                        putExtra("email", getEmail())
                        putExtra("author", model.author)
                        putExtra("authorName", model.name)
                        putExtra("question", model.id)

                    }//answerIntent

                    startActivity(answerIntent)

                }//setOnClickListener

                val txtTitleText : TextView = holder.itemView.findViewById(R.id.txtTitleText)
                val txtItemDescription : TextView = holder.itemView.findViewById(R.id.txtItemDescription)
                val txtAuthor : TextView = holder.itemView.findViewById(R.id.txtAuthor)

                txtTitleText.text = model.title
                txtItemDescription.text = model.description
                txtAuthor.text = model.name

            }//onBindViewHolder

        }//adapter

        binding.rvHome.adapter = adapter
        binding.rvHome.layoutManager = LinearLayoutManager(context)

    }//show questions

    private fun updateQuestion(model: Questions){

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

    }//update question

    private fun reactions(model: Questions, mainArray: ArrayList<String>, secondArray: ArrayList<String>, txtReaction: TextView,  text: String, author: String, title: String, id:String){

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

            updateQuestion(model)

        }//setOnClickListener

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

    private fun getEmail() : String?{

        val prefs : SharedPreferences =  requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
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

}//class home fragment