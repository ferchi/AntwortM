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

                //--------------------------------------------- Programación de los likes --------------------------------------------- //

                if(model.likes.isNotEmpty()){

                    holder.questionBinding.likesIQ.text = model.likes.size.toString()

                    if(model.likes.contains(getEmail()) && !model.dislikes.contains(getEmail())) {

                        holder.questionBinding.likesIQ.compoundDrawables[0].setTint(Color.parseColor("#FB771E"))
                        holder.questionBinding.likesIQ.setTextColor(Color.parseColor("#FB771E"))

                    }//el usuario le dio like a su post

                    else {

                        holder.questionBinding.likesIQ.compoundDrawables[0].setTint(-1979711488)
                        holder.questionBinding.likesIQ.setTextColor(-1979711488)

                    }//el usuario no le dio like al post

                }//tiene likes

                else{

                    holder.questionBinding.likesIQ.text = "Útil"
                    holder.questionBinding.likesIQ.compoundDrawables[0].setTint(-1979711488)
                    holder.questionBinding.likesIQ.setTextColor(-1979711488)

                }//no tiene likes

                holder.questionBinding.likesIQ.setOnClickListener {

                    if(model.likes.contains(getEmail())){

                        model.likes.remove(getEmail().toString())

                    }//si mi correo esta en la lista de likes

                    else{

                        if(model.dislikes.contains(getEmail())){

                            model.dislikes.remove(getEmail().toString())

                        }// si estoy es la lista de dislikes eliminamos mi correo

                        if(getEmail() != model.author){

                            createNotification("Han reaccionado a tu pregunta:", model.title, model.id, model.author)
                            showNotification("Han reaccionado a tu pregunta:", model.title, model.id, model.author)

                        }//creamos notificacion solo si no estoy dandome like a mi mismo

                        model.likes.add(getEmail().toString())

                    }// mi correo no esta en la lista de likes

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

                }//setOnClickListener

                //--------------------------------------------- Programación de los dislikes --------------------------------------------- //

                if(model.dislikes.isNotEmpty()){

                    holder.questionBinding.dislikesIQ.text = model.dislikes.size.toString()

                    if(model.dislikes.contains(getEmail()) && !model.likes.contains(getEmail())) {

                        holder.questionBinding.dislikesIQ.compoundDrawables[0].setTint(Color.parseColor("#FB771E"))
                        holder.questionBinding.dislikesIQ.setTextColor(Color.parseColor("#FB771E"))

                    }//el usuario le dio like a su post

                    else {

                        holder.questionBinding.dislikesIQ.compoundDrawables[0].setTint(-1979711488)
                        holder.questionBinding.dislikesIQ.setTextColor(-1979711488)

                    }//el usuario no le dio like al post

                }//tiene likes

                else{

                    holder.questionBinding.dislikesIQ.text = "No útil"
                    holder.questionBinding.dislikesIQ.compoundDrawables[0].setTint(-1979711488)
                    holder.questionBinding.dislikesIQ.setTextColor(-1979711488)

                }//no tiene likes

                holder.questionBinding.dislikesIQ.setOnClickListener {

                    if(model.dislikes.contains(getEmail())){

                        model.dislikes.remove(getEmail().toString())

                    }//si mi correo esta en la lista de dislikes

                    else{

                        if(model.likes.contains(getEmail())){

                            model.likes.remove(getEmail().toString())

                        }//eliminiamos el correo de la lista de likes

                        //Crear la notificación

                        if(getEmail() != model.author){

                            createNotification("Han reaccionado a tu pregunta: ", model.title, model.id, model.author)
                            showNotification("Han reaccionado a tu pregunta", model.title, model.id, model.author)

                        }//creamos notificacion solo si no estoy dandome like a mi mismo

                        model.dislikes.add(getEmail().toString())

                    }// mi correo no esta en la lista de likes

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

                }//setOnClickListener

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

    private fun showNotification(title: String, message: String, question: String, email:String){

        db.collection("Users").document(email).get().addOnSuccessListener {

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

    private fun createNotification(title: String, content: String, question: String, author: String) {

        val id: String = db.collection("Notifications").document().id

        db.collection("Notifications").document(id).set(

            hashMapOf(

                "id" to id,
                "title" to title,
                "author" to getEmail(),
                "content" to content,
                "question" to question,
                "user" to author

            )//hashMapOf con los nuevos datos

        )//creamos la notificacion

    }//createNotification

}//class home fragment