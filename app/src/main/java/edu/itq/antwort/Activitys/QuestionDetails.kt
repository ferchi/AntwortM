package edu.itq.antwort.Activitys

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
import com.google.gson.Gson
import edu.itq.antwort.Classes.*
import edu.itq.antwort.databinding.ItemAnswerViewBinding
import edu.itq.antwort.databinding.ItemQuestionViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

                //--------------------------------------------- Programación de likes --------------------------------------------- //

                if(model.likes.isNotEmpty()){

                    holder.questionViewBinding.txtLikeQD.text = model.likes.size.toString()
                    if(model.likes.contains(user) && !model.dislikes.contains(user)) {

                        holder.questionViewBinding.txtLikeQD.compoundDrawables[0].setTint(Color.parseColor("#FB771E"))
                        holder.questionViewBinding.txtLikeQD.setTextColor(Color .parseColor("#FB771E"))

                    }//if le di like a esta publicación

                    else {

                        holder.questionViewBinding.txtLikeQD.compoundDrawables[0].setTint(-1979711488)
                        holder.questionViewBinding.txtLikeQD.setTextColor(-1979711488)

                    }//el usuario no le dio like a su post

                }//los likes no estan vacios

                else{

                    holder.questionViewBinding.txtLikeQD.text = "Útil"
                    holder.questionViewBinding.txtLikeQD.compoundDrawables[0].setTint(-1979711488)
                    holder.questionViewBinding.txtLikeQD.setTextColor(-1979711488)

                }//no tiene likes

                holder.questionViewBinding.txtLikeQD.setOnClickListener {

                    if(model.likes.contains(user)){

                        model.likes.remove(user)

                    }//si mi correo esta en la lista de likes

                    else{

                        if(model.dislikes.contains(user)){

                            model.dislikes.remove(user)

                        }// si estoy es la lista de dislikes eliminamos mi correo

                        if(user != model.author){

                            createNotification("Han reaccionado a tu pregunta:", model.title, user, model.id, model.author)
                            showNotification("Han reaccionado a tu pregunta:", model.title, model.id, model.author)

                        }//creamos notificacion solo si no estoy dandome like a mi mismo

                        model.likes.add(user)

                    }// mi correo no esta en la lista de likes

                    db.collection("Questions").document(model.id).set(

                        hashMapOf(

                            "id" to model.id,
                            "name" to model.name,
                            "date" to model.date,
                            "author" to model.author,
                            "title" to model.title,
                            "description" to model.description,
                            "likes" to model.likes,

                            )//hashMapOf con los nuevos datos

                    )//actualizamos el numero de likes

                }//setOnClickListener txtLikeQD

                //--------------------------------------------- Programación de dislikes --------------------------------------------- //

                if(model.dislikes.isNotEmpty()){

                    holder.questionViewBinding.txtDislikeQD.text = model.dislikes.size.toString()
                    if(model.dislikes.contains(user) && !model.likes.contains(user)) {

                        holder.questionViewBinding.txtDislikeQD.compoundDrawables[0].setTint(Color.parseColor("#FB771E"))
                        holder.questionViewBinding.txtDislikeQD.setTextColor(Color.parseColor("#FB771E"))

                    }//if le di like a esta publicación

                    else {

                        holder.questionViewBinding.txtDislikeQD.compoundDrawables[0].setTint(-1979711488)
                        holder.questionViewBinding.txtDislikeQD.setTextColor(-1979711488)

                    }//el usuario no le dio like a su post

                }//los likes no estan vacios

                else{

                    holder.questionViewBinding.txtDislikeQD.text = "No útil"
                    holder.questionViewBinding.txtDislikeQD.compoundDrawables[0].setTint(-1979711488)
                    holder.questionViewBinding.txtDislikeQD.setTextColor(-1979711488)

                }//no tiene likes

                holder.questionViewBinding.txtDislikeQD.setOnClickListener {

                    if(model.dislikes.contains(user)){

                        model.dislikes.remove(user)

                    }//si mi correo esta en la lista de likes

                    else{

                        if(model.likes.contains(user)){

                            model.likes.remove(user)

                        }//eliminiamos el correo de la lista de likes

                        if(user != model.author){

                            createNotification("Han reaccionado a tu pregunta:", model.title, user, model.id, model.author)
                            showNotification("Han reaccionado a tu pregunta:", model.title, model.id, model.author)

                        }//creamos notificacion solo si no estoy dandome like a mi mismo

                        model.dislikes.add(user)

                    }// mi correo no esta en la lista de likes

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

                }//setOnClickListener txtLikeQD

                //--------------------------------------------- Programación del boton responder --------------------------------------------- //

                holder.questionViewBinding.txtAnswersQD.setOnClickListener {

                    answer(user, id, model.author)

                }//setOnClickListener txtAnswersQD

                val txtNameQD : TextView = holder.itemView.findViewById(R.id.txtNameQD)
                val txtTitleQD : TextView = holder.itemView.findViewById(R.id.txtTitleQD)
                val txtDescriptionQD : TextView = holder.itemView.findViewById(R.id.txtDescriptionQD)

                txtNameQD.text = model.name
                txtTitleQD.text = model.title
                txtDescriptionQD.text = model.description

            }//onBindViewHolder

        }//adapter question

        binding.rvQD.adapter = adapter
        binding.rvQD.layoutManager = LinearLayoutManager(this)

    }//showQuestion

    private fun showAnswers(id: String, user: String){

        val queryAnswers = db.collection("Answers").whereEqualTo("question", id)
        val answerOptions = FirestoreRecyclerOptions.Builder<Answers>().setQuery(queryAnswers, Answers::class.java).setLifecycleOwner(this).build()

        val answerAdapter = object: FirestoreRecyclerAdapter<Answers, AnswerDetailViewHolder>(answerOptions){

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerDetailViewHolder {

                return AnswerDetailViewHolder(ItemAnswerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))

            }//onCreateViewHolder

            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(holder: AnswerDetailViewHolder, position: Int, model: Answers) {

                holder.answerViewBinding.imgUserAV.setOnClickListener {

                    val intent = Intent(this@QuestionDetails, ProfileActivity::class.java).apply {

                        putExtra("author", model.author)

                    }//intent

                    startActivity(intent)

                }//si se presiona la foto de perfil te lleva al perfil del usuario

                // --------------------------------------------- Programación de likes --------------------------------------------- //

                if(model.likes.isNotEmpty()){

                    holder.answerViewBinding.likesIQ.text = model.likes.size.toString()

                    if(model.likes.contains(user) && !model.dislikes.contains(user)) {

                        holder.answerViewBinding.likesIQ.compoundDrawables[0].setTint(Color.parseColor("#FB771E"))
                        holder.answerViewBinding.likesIQ.setTextColor(Color.parseColor("#FB771E"))

                    }//if le di like a esta publicación

                    else {

                        holder.answerViewBinding.likesIQ.compoundDrawables[0].setTint(-1979711488)
                        holder.answerViewBinding.likesIQ.setTextColor(-1979711488)

                    }//el usuario no le dio like a su post

                }//los likes no estan vacios

                else{

                    holder.answerViewBinding.likesIQ.text = "Útil"
                    holder.answerViewBinding.likesIQ.compoundDrawables[0].setTint(-1979711488)
                    holder.answerViewBinding.likesIQ.setTextColor(-1979711488)

                }//no tiene likes

                holder.answerViewBinding.likesIQ.setOnClickListener {

                    if(model.likes.contains(user)){

                        model.likes.remove(user)

                    }//si mi correo esta en la lista de likes

                    else{

                        if(model.dislikes.contains(user)){

                            model.dislikes.remove(user)

                        }// si estoy es la lista de dislikes eliminamos mi correo

                        if(user != model.author){

                            db.collection("Answers").document(model.id).get().addOnSuccessListener {

                                createNotification("Han reaccionado a tu respuesta:", it.get("content") as String, user, model.question, model.author)
                                showNotification("Han reaccionado a tu respuesta:", it.get("content") as String, model.question, model.author)

                            }//obtenemos el contenido de la respuesta

                        }//creamos notificacion solo si no estoy dandome like a mi mismo

                        model.likes.add(user)

                    }// mi correo no esta en la lista de likes

                    db.collection("Answers").document(model.id).set(

                        hashMapOf(

                            "id" to model.id,
                            "date" to model.date,
                            "author" to model.author,
                            "content" to model.content,
                            "likes" to model.likes,
                            "question" to model.question,
                            "dislikes" to model.dislikes

                        )//hashMapOf con los nuevos datos

                    )//actualizamos el numero de likes

                }//setOnClickListener txtLikeQD

                // --------------------------------------------- Programación de dislikes --------------------------------------------- //

                if(model.dislikes.isNotEmpty()){

                    holder.answerViewBinding.dislikeIQ.text = model.dislikes.size.toString()
                    if(model.dislikes.contains(user) && !model.likes.contains(user)) {

                        holder.answerViewBinding.dislikeIQ.compoundDrawables[0].setTint(Color.parseColor("#FB771E"))
                        holder.answerViewBinding.dislikeIQ.setTextColor(Color.parseColor("#FB771E"))

                    }//if le di like a esta publicación

                    else {

                        holder.answerViewBinding.dislikeIQ.compoundDrawables[0].setTint(-1979711488)
                        holder.answerViewBinding.dislikeIQ.setTextColor(-1979711488)

                    }//el usuario no le dio like a su post

                }//los likes no estan vacios

                else{

                    holder.answerViewBinding.dislikeIQ.text = "No útil"
                    holder.answerViewBinding.dislikeIQ.compoundDrawables[0].setTint(-1979711488)
                    holder.answerViewBinding.dislikeIQ.setTextColor(-1979711488)

                }//no tiene likes

                holder.answerViewBinding.dislikeIQ.setOnClickListener {

                    if(model.dislikes.contains(user)){

                        model.dislikes.remove(user)

                    }//si mi correo esta en la lista de likes

                    else{

                        if(model.likes.contains(user)){

                            model.likes.remove(user)

                        }//eliminiamos el correo de la lista de likes

                        if(user != model.author){

                            db.collection("Answers").document(model.id).get().addOnSuccessListener {

                                createNotification("Han reaccionado a tu respuesta:", it.get("content") as String, user, model.question, model.author)
                                showNotification("Han reaccionado a tu respuesta:", it.get("content") as String, model.question, model.author)

                            }//obtenemos el contenido de la respuesta

                        }//creamos notificacion solo si no estoy dandome like a mi mismo

                        model.dislikes.add(user)

                    }// mi correo no esta en la lista de likes

                    db.collection("Answers").document(model.id).set(

                        hashMapOf(

                            "id" to model.id,
                            "author" to model.author,
                            "date" to model.date,
                            "content" to model.content,
                            "likes" to model.likes,
                            "question" to model.question,
                            "dislikes" to model.dislikes

                        )//hashMapOf con los nuevos datos

                    )//actualizamos el numero de likes

                }//setOnClickListener txtLikeQD

                //--------------------------------------------- Mostrar los datos obtenidos --------------------------------------------- //

                val txtNameAV : TextView = holder.itemView.findViewById(R.id.txtNameAV)
                val txtAnswersAV : TextView = holder.itemView.findViewById(R.id.txtAnswersAV)

                db.collection("Users").document(model.author).get().addOnSuccessListener {
                    txtNameAV.text = it.get("name") as String?
                }//obtener nombre del usuario que publicó

                txtAnswersAV.text = model.content

            }//onBindViewHolder

        }//answerAdapter

        binding.rvAD.adapter = answerAdapter
        binding.rvAD.layoutManager = LinearLayoutManager(this)

    }//showAnswers

    private fun sendNotifiaction(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {

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

                    sendNotifiaction(it)

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

}//class

