package edu.itq.antwort.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ActivityQuestionViewBinding
import android.content.Intent
import android.util.Log
import android.view.View
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import edu.itq.antwort.Classes.*
import edu.itq.antwort.databinding.ItemAnswerViewBinding
import edu.itq.antwort.databinding.ItemQuestionViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class QuestionDetailViewHolder(val questionViewBinding: ItemQuestionViewBinding) : RecyclerView.ViewHolder(questionViewBinding.root)
class AnswerDetailViewHolder(val answerViewBinding: ItemAnswerViewBinding) : RecyclerView.ViewHolder(answerViewBinding.root)

const val TAG = "QuestionsDetails"

class QuestionDetails : AppCompatActivity() {

    lateinit var binding: ActivityQuestionViewBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        requireNotNull(this).application
        binding = ActivityQuestionViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bundle = intent.extras
        val id = bundle?.getString("id")

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val user = prefs.getString("email", null)

        showQuestion(user?:"", id?:"")
        showAnswers(id?:"", user?:"")

        setup()

    }//onCreate

    private fun setup(){

        binding.includeQuestionToolbar.imgBackTBQ.setOnClickListener {

            onBackPressed()

        }//imgBackTBQ.setOnClickListener

    }//setup

    private fun answer(email: String, id: String, author: String){

        val answerIntent = Intent(this, AnswerScreenActivity::class.java).apply {

            putExtra("email", email)
            putExtra("author", author)
            putExtra("question", id)

        }//answerIntent

        startActivity(answerIntent)

    }//función showHome

    private fun showQuestion(user: String, id: String){

        val query = db.collection("Questions").whereEqualTo("id", id)
        val options = FirestoreRecyclerOptions.Builder<Questions>().setQuery(query, Questions::class.java).setLifecycleOwner(this).build()

        val adapter = object: FirestoreRecyclerAdapter<Questions, QuestionDetailViewHolder>(options){

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionDetailViewHolder {

                return QuestionDetailViewHolder(ItemQuestionViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))

            }//onCreateViewHolder

            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(holder: QuestionDetailViewHolder, position: Int, model: Questions) {

                holder.questionViewBinding.imgUserQD.setOnClickListener {

                    val intent = Intent(this@QuestionDetails, ProfileActivity::class.java).apply {

                        putExtra("author", model.author)

                    }//intent

                    startActivity(intent)

                }//si se presiona la foto de perfil te lleva al perfil del usuario

                //--------------------------------------------- Programación de las reacciones --------------------------------------------- //

                reactions(null, model, model.likes, model.dislikes, holder.questionViewBinding.txtLikeQD, user, model.author, model.id, model.id, "Han reaccionado a tu pregunta", "Questions", "title")
                reactions(null, model, model.dislikes, model.likes, holder.questionViewBinding.txtDislikeQD, user, model.author, model.id, model.id, "Han reaccionado a tu pregunta", "Questions", "title")

                //--------------------------------------------- Programación del boton responder --------------------------------------------- //

                holder.questionViewBinding.txtAnswersQD.setOnClickListener {

                    answer(user, id, model.author)

                }//setOnClickListener txtAnswersQD

                val txtNameQD : TextView = holder.itemView.findViewById(R.id.txtNameQD)
                val txtTitleQD : TextView = holder.itemView.findViewById(R.id.txtTitleQD)
                val txtDescriptionQD : TextView = holder.itemView.findViewById(R.id.txtDescriptionQD)
                val txtAnswersQD : TextView = holder.itemView.findViewById(R.id.txtAnswersQD)
                val imgUserQD : CircleImageView = holder.itemView.findViewById(R.id.imgUserQD)

                loadImg(imgUserQD, model.author)
                txtAnswersQD.text = model.answers.toString()
                txtNameQD.text = model.name
                txtTitleQD.text = model.title
                txtDescriptionQD.text = model.description

            }//onBindViewHolder

        }//adapter question

        binding.rvQD.adapter = adapter
        binding.rvQD.layoutManager = LinearLayoutManager(this)

    }//showQuestion

    private fun showAnswers(id: String, user: String){

        val queryAnswers = db.collection("Answers").whereEqualTo("question", id).orderBy("date", Query.Direction.DESCENDING)
        val answerOptions = FirestoreRecyclerOptions.Builder<Answers>().setQuery(queryAnswers, Answers::class.java).setLifecycleOwner(this).build()

        val answerAdapter = object: FirestoreRecyclerAdapter<Answers, AnswerDetailViewHolder>(answerOptions){

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerDetailViewHolder {

                return AnswerDetailViewHolder(ItemAnswerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))

            }//onCreateViewHolder

            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(holder: AnswerDetailViewHolder, position: Int, model: Answers) {
/*
                db.collection("Users").document(model.author).get().addOnSuccessListener {

                    if(it.get("rol") as String? == "facilitador") {

                        holder.answerViewBinding.imgVerifiedUser.visibility = View.VISIBLE

                    }//tiene el rol de facilitador

                }//revisamos si el usuario tiene el rol de facilitador
                */
                if(model.verified){

                    holder.answerViewBinding.imgVerifiedUser.visibility = View.VISIBLE

                }//mostramos el verificado

                holder.answerViewBinding.imgUserAV.setOnClickListener {

                    val intent = Intent(this@QuestionDetails, ProfileActivity::class.java).apply {

                        putExtra("author", model.author)

                    }//intent

                    startActivity(intent)

                }//si se presiona la foto de perfil te lleva al perfil del usuario

                //Llamamos las funciones necesarias para likes y dislikes

                reactions(model,null, model.likes, model.dislikes, holder.answerViewBinding.likesIA, user, model.author, model.id, model.question, "Han reaccionado a tu respuesta", "Answers", "content")
                reactions(model,null, model.dislikes, model.likes, holder.answerViewBinding.dislikeIA, user, model.author, model.id, model.question, "Han reaccionado a tu respuesta", "Answers", "content")

                //Mostramos los datos obtenidos

                val txtNameAV : TextView = holder.itemView.findViewById(R.id.txtNameAV)
                val txtAnswersAV : TextView = holder.itemView.findViewById(R.id.txtAnswersAV)
                val imgUserAV : CircleImageView = holder.itemView.findViewById(R.id.imgUserAV)

                loadImg(imgUserAV, model.author)
                txtNameAV.text = model.nameAuthor
                txtAnswersAV.text = model.content

            }//onBindViewHolder

        }//answerAdapter

        binding.rvAD.adapter = answerAdapter
        binding.rvAD.layoutManager = LinearLayoutManager(this)

    }//showAnswers

    private fun loadImg(image : CircleImageView, author: String) {

        db.collection("Users").document(author).addSnapshotListener{
                result, error ->
            val urlImg = result!!.get("imgProfile").toString()

            try {
                Picasso.get().load(urlImg).into(image)

            } catch (e: Exception) {
                Picasso.get().load(R.drawable.ic_user_profile).into(image)
            }
        }
    }

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

    private fun updateAnswers(model: Answers){

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

    private fun reactions(modelAnswers: Answers?, modelQuestions: Questions?, mainArray: ArrayList<String>, secondArray: ArrayList<String>, txtReaction: TextView, user: String, author: String, id: String, question: String, title: String, collection: String, content: String){

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

            txtReaction.text = ""
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

                updateAnswers(modelAnswers)

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

}//class