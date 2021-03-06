package edu.itq.antwort.Activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import edu.itq.antwort.Adapters.FileAdapter
import edu.itq.antwort.Classes.*
import edu.itq.antwort.Methods
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
    private lateinit var popUpMenu: PowerMenu.Builder
    private var q: String = ""
    private var a: String = ""
    private var parent: String = ""
    private var collec: String = ""
    private var user: String = ""
    private var questionAuthor: String = ""
    private lateinit var filesFolder : StorageReference
    private  var fileNames : ArrayList<String> = arrayListOf()
    private  var references : MutableList<StorageReference> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        requireNotNull(this).application
        binding = ActivityQuestionViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        popUpMenu = PowerMenu.Builder(this)
        val bundle = intent.extras
        val id = bundle?.getString("id")

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        user = prefs.getString("email", null)!!

        concatAdapter(showQuestion(user, id?:""), showAnswers(id?:"", user))

        setup()
        db.collection("Users").document(Methods.getEmail(this)!!).get().addOnSuccessListener {

            postAnswer(id!!, it.get("rol") as String, it.get("name") as String)

        }//obtenemos el rol del usuario

    }//onCreate

    @SuppressLint("ClickableViewAccessibility")
    private fun setup(){

        binding.newAnswer.setOnTouchListener(object : View.OnTouchListener {

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                binding.btnPostAnswers.visibility = View.VISIBLE

                if(binding.newAnswer.hasFocus()){

                    v?.parent?.requestDisallowInterceptTouchEvent(true)

                    when(event?.action){

                        MotionEvent.ACTION_SCROLL -> {
                            v?.parent?.requestDisallowInterceptTouchEvent(false)
                            return true

                        }//MotionEvent.ACTION_SCROLL

                    }//when

                }//tiene focus

                return false

            }//onTouch

        })//setOnTouchListener

        binding.includeQuestionToolbar.imgBackTBQ.setOnClickListener {

            onBackPressed()

        }//imgBackTBQ.setOnClickListener

    }//setup

    private fun postAnswer(question: String, rol: String, name: String){

        binding.btnPostAnswers.setOnClickListener {

            if(binding.newAnswer.text.isNotEmpty()){

                val id: String = db.collection("Answers").document().id
                val timestamp: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
                val likes: ArrayList<String> = ArrayList()
                val dislikes: ArrayList<String> = ArrayList()

                db.collection("Answers").document(id).set(

                    hashMapOf(

                        "id" to id,
                        "nameAuthor" to name,
                        "date" to timestamp,
                        "author" to Methods.getEmail(this)!!,
                        "likes" to likes,
                        "dislikes" to dislikes,
                        "verified" to (rol == "Facilitador"),
                        "content" to binding.newAnswer.text.toString(),
                        "question" to question

                    )

                )//guardamos la nueva respuesta

                db.collection("Users").document(Methods.getEmail(this)!!).update("answers", FieldValue.increment(1))
                db.collection("Questions").document(question).update("answers", FieldValue.increment(1))
                binding.newAnswer.setText("")
                hideKeyboard(this)
                binding.btnPostAnswers.visibility = View.GONE
                binding.newAnswer.clearFocus()

                if(questionAuthor != Methods.getEmail(this)!!){

                    db.collection("Questions").document(question).get().addOnSuccessListener {

                        createNotification("$name ha respondido tu pregunta:", it.get("title") as String, Methods.getEmail(this)!!, question, questionAuthor, timestamp)
                        showNotification("$name ha respondido tu pregunta:", it.get("title") as String,question,  questionAuthor)

                    }//addOnSuccessListener

                }//el autor de la pregunta y la respuesta son diferentes, por lo tanto enviamos notificacion

            }//la respuesta no esta vacia, posteamos la respuesta

            else{

                Toast.makeText(this, "Rellene el campo de respusta", Toast.LENGTH_SHORT).show()
                binding.newAnswer.requestFocus()

            }//el campo de respuesta esta vacio

        }//setOnClickListener

    }//post answer

    private fun hideKeyboard(context: AppCompatActivity){

        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(context.currentFocus!!.windowToken, 0)

    }//hideKeyboard

    private fun answer(email: String, id: String, author: String){

        val answerIntent = Intent(this, AnswerScreenActivity::class.java).apply {

            putExtra("email", email)
            putExtra("author", author)
            putExtra("question", id)

        }//answerIntent

        startActivity(answerIntent)

    }//funci??n showHome

    private fun showQuestion(user: String, id: String): FirestoreRecyclerAdapter<Questions, QuestionDetailViewHolder> {

        val query = db.collection("Questions").whereEqualTo("id", id)
        val options = FirestoreRecyclerOptions.Builder<Questions>().setQuery(query, Questions::class.java).setLifecycleOwner(this).build()

        val adapter = object: FirestoreRecyclerAdapter<Questions, QuestionDetailViewHolder>(options){

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionDetailViewHolder {

                return QuestionDetailViewHolder(ItemQuestionViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))

            }//onCreateViewHolder

            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(holder: QuestionDetailViewHolder, position: Int, model: Questions) {

                questionAuthor = model.author

                holder.questionViewBinding.imgOptionQV.setOnClickListener {

                    popUpMenu.build().clearPreference()

                    if(user == model.author){

                        createPopUpOwner()

                    }//el usuario es el autor de la pregunta

                    else{

                        createPopUp()

                    }//no es su pregunta

                    popUpMenu.build().showAsDropDown(it)
                    q = model.id
                    a = model.author
                    collec = "Questions"

                }//se presiono el boton de opciones


                filesFolder = FirebaseStorage.getInstance().reference.child("Files/${model.id}")
                filesFolder.listAll().addOnCompleteListener {
                    fileNames.clear()
                    references.clear()

                    it.result!!.items.forEach { file ->
                        fileNames.add(file.name)
                        references.add(file)
                    }

                    val fileAdapter = FileAdapter(this@QuestionDetails, fileNames, references)

                    holder.questionViewBinding.rvFiles.apply {
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
                        adapter = fileAdapter
                    }
                }

                if(Methods.getEmail(this@QuestionDetails).toString() != (model.author)) {

                    holder.questionViewBinding.imgUserQD.setOnClickListener {
                        val intent = Intent(this@QuestionDetails, ProfileActivity::class.java).apply {
                            putExtra("email", model.author)
                        }//intent
                        startActivity(intent)
                    }//si se presiona la foto de perfil te lleva al perfil del usuario
                }

                //--------------------------------------------- Programaci??n de las reacciones --------------------------------------------- //

                reactions(null, model, model.likes, model.dislikes, holder.questionViewBinding.txtLikeQD, user, model.author, model.id, model.id, "Han reaccionado a tu pregunta", "Questions", "title")
                reactions(null, model, model.dislikes, model.likes, holder.questionViewBinding.txtDislikeQD, user, model.author, model.id, model.id, "Han reaccionado a tu pregunta", "Questions", "title")

                //--------------------------------------------- Programaci??n del boton responder --------------------------------------------- //

                holder.questionViewBinding.txtAnswersQD.setOnClickListener {

                    answer(user, id, model.author)

                }//setOnClickListener txtAnswersQD

                val txtNameQD : TextView = holder.itemView.findViewById(R.id.txtNameQD)
                val txtTitleQD : TextView = holder.itemView.findViewById(R.id.txtTitleQD)
                val txtDescriptionQD : TextView = holder.itemView.findViewById(R.id.txtDescriptionQD)
                val txtAnswersQD : TextView = holder.itemView.findViewById(R.id.txtAnswersQD)
                val imgUserQD : CircleImageView = holder.itemView.findViewById(R.id.imgUserQD)

                loadImg(imgUserQD, model.author)

                if(model.answers > 0)
                    txtAnswersQD.text = model.answers.toString()

                else
                    txtAnswersQD.text = ""

                txtNameQD.text = model.name
                txtTitleQD.text = model.title
                txtDescriptionQD.text = model.description

                if(model.edited){
                    holder.questionViewBinding.tvItemQuestionEdit.visibility = View.VISIBLE
                }

                if ((holder.questionViewBinding.chipGroupItemQuestion.childCount) == 0){
                    model.topics.forEach {
                        addTag(it, holder)
                    }
                }

            }//onBindViewHolder

        }//adapter question

        return adapter

    }//showQuestion

    @SuppressLint("InflateParams")
    private fun addTag(s: CharSequence, holder: QuestionDetailViewHolder) {
        val layoutInflater = LayoutInflater.from(this)
        val tag = layoutInflater.inflate(R.layout.item_topic_show, null, false) as Chip
        tag.text = s
        holder.questionViewBinding.chipGroupItemQuestion.addView(tag)
    }

    private fun showAnswers(id: String, user: String): FirestoreRecyclerAdapter<Answers, AnswerDetailViewHolder> {

        val queryAnswers = db.collection("Answers").whereEqualTo("question", id).orderBy("date", Query.Direction.DESCENDING)
        val answerOptions = FirestoreRecyclerOptions.Builder<Answers>().setQuery(queryAnswers, Answers::class.java).setLifecycleOwner(this).build()

        val answerAdapter = object: FirestoreRecyclerAdapter<Answers, AnswerDetailViewHolder>(answerOptions){

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerDetailViewHolder {

                return AnswerDetailViewHolder(ItemAnswerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))

            }//onCreateViewHolder

            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(holder: AnswerDetailViewHolder, position: Int, model: Answers) {

                if(model.verified){

                    holder.answerViewBinding.imgVerifiedUser.visibility = View.VISIBLE

                }//mostramos el verificado

                else{

                    holder.answerViewBinding.imgVerifiedUser.visibility = View.INVISIBLE

                }//no esta verificado

                holder.answerViewBinding.imgOptionAV.setOnClickListener {

                    popUpMenu.build().clearPreference()

                    if(user == model.author){

                        createPopUpOwner()

                    }//el usuario es el autor de la pregunta

                    else{

                        createPopUp()

                    }//no es su pregunta

                    popUpMenu.build().showAsDropDown(it)
                    q = model.id
                    a = model.author
                    parent = model.question
                    collec = "Answers"

                }//se presiono el boton de opciones

                if(Methods.getEmail(this@QuestionDetails).toString() != (model.author)) {
                    holder.answerViewBinding.imgUserAV.setOnClickListener {

                        val homeIntent = Intent(this@QuestionDetails, ProfileActivity::class.java).apply {

                                putExtra("email", model.author)

                            }//homeIntent
                        startActivity(homeIntent)
                    }
                }

                if(model.edited){
                    holder.answerViewBinding.ivItemAnswerEdit.visibility = View.VISIBLE
                }

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

        return answerAdapter

    }//showAnswers

    private fun concatAdapter(question: FirestoreRecyclerAdapter<Questions, QuestionDetailViewHolder>, answers : FirestoreRecyclerAdapter<Answers, AnswerDetailViewHolder>){

        if(answers == null){

            binding.rvQD.adapter = question

        }//no hay respuestas

        else{

            binding.rvQD.adapter = ConcatAdapter(question, answers)

        }//s?? hay respuestas

        binding.rvQD.layoutManager = Methods.LinearLayoutManagerWrapper(this)

    }//concatAdapter

    private fun loadImg(image : CircleImageView, author: String) {

        db.collection("Users").document(author).get().addOnSuccessListener {

            val img = it.get("imgProfile") as String?

            try{

                if(img!!.isNotEmpty()){

                    Picasso.get().load(img).into(image)

                }//la imagen no esta vacia

            }catch (e: Exception) {

                Picasso.get().load(R.drawable.ic_user_profile).into(image)

            }//try-catch

        }//obtenemos la foto del perfil

    }//loadImage

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {

        try {

            val response = RetrofitInstance.api.postNotification(notification)

            if(response.isSuccessful){

                //Log.d(TAG, "Response : ${Gson().toJson(response)}")

            }//if isSuccessful

            else{

                //response.errorBody()?.let { Log.e(TAG, it.toString()) }

            }//else

        }catch (e: Exception) {


            //Log.e(TAG, e.toString())

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

    private fun createNotification(title: String, content: String, user:String, question: String, author: String, timestamp: com.google.firebase.Timestamp) {

        val id: String = db.collection("Notifications").document().id

        db.collection("Notifications").document(id).set(

            hashMapOf(

                "id" to id,
                "title" to title,
                "author" to user,
                "content" to content,
                "question" to question,
                "user" to author,
                "date" to timestamp

            )//hashMapOf con los nuevos datos

        )//creamos la notificacion

    }//createNotification

    private fun updateAnswers(model: Answers){

        db.collection("Answers").document(model.id).update("likes", model.likes)
        db.collection("Answers").document(model.id).update("dislikes", model.dislikes)


    }//update answers

    private fun updateQuestions(model: Questions){

        db.collection("Questions").document(model.id).update("likes", model.likes)
        db.collection("Questions").document(model.id).update("dislikes", model.dislikes)

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

                        val timestamp: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
                        createNotification(title, it.get(content) as String, user, question, author, timestamp)
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

            when(txtReaction.id){

                R.id.txtLikeQD, R.id.likesIA -> {
                    txtReaction.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_baseline_thumb_up_24,
                        0,
                        0,
                        0
                    )
                }
                R.id.txtDislikeQD, R.id.dislikeIA -> {
                    txtReaction.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_baseline_thumb_down_24,
                        0,
                        0,
                        0
                    )
                }
            }

            txtReaction.compoundDrawables[0].setTint(Color.parseColor("#FFFFFFFF"))
            txtReaction.setTextColor(Color.parseColor("#FFFFFFFF"))

        }// se le dio like

        else{
            when(txtReaction.id){

                R.id.txtLikeQD, R.id.likesIA -> {
                    txtReaction.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_outline_thumb_up_24,
                        0,
                        0,
                        0
                    )
                }
                R.id.txtDislikeQD, R.id.dislikeIA -> {
                    txtReaction.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_outline_thumb_down_24,
                        0,
                        0,
                        0
                    )
                }
            }

            txtReaction.compoundDrawables[0].setTint(Color.parseColor("#FFFFFFFF"))
            txtReaction.setTextColor((Color.parseColor("#FFFFFFFF")))

        }//else se le dio dislike

    }//reaction color

    private fun createPopUpOwner(){

        popUpMenu = PowerMenu.Builder(this)
        popUpMenu
            .addItem(PowerMenuItem("Editar", false))
            .addItem(PowerMenuItem("Eliminar", false))
            .setAnimation(MenuAnimation.FADE) // Animation start point (TOP | LEFT).
            .setMenuRadius(10f) // sets the corner radius.
            .setMenuShadow(10f) // sets the shadow.
            .setTextColor(ContextCompat.getColor(this, R.color.black))
            .setTextGravity(Gravity.CENTER)
            .setSelectedTextColor(Color.WHITE)
            .setMenuColor(Color.WHITE)
            .setSelectedMenuColor(ContextCompat.getColor(this, R.color.orange))
            .setOnMenuItemClickListener(onMenuItemClickListenerOwner)
            .setAutoDismiss(true)
            .build()

    }//createPopUpOwner

    private fun createPopUp(){

        popUpMenu = PowerMenu.Builder(this)
        popUpMenu
            .addItem(PowerMenuItem("Reportar", false))
            .setAnimation(MenuAnimation.FADE) // Animation start point (TOP | LEFT).
            .setMenuRadius(10f) // sets the corner radius.
            .setMenuShadow(10f) // sets the shadow.
            .setTextColor(ContextCompat.getColor(this, R.color.black))
            .setTextGravity(Gravity.CENTER)
            .setSelectedTextColor(Color.WHITE)
            .setMenuColor(Color.WHITE)
            .setSelectedMenuColor(ContextCompat.getColor(this, R.color.orange))
            .setOnMenuItemClickListener(onMenuItemClickListener)
            .setAutoDismiss(true)
            .build()

    }//create PopUp

    private fun showAlert(collection: String){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar publicaci??n")
        builder.setMessage("??Esta seguro que desea elimiar la publicaci??n?")

        builder.setPositiveButton("S??"
        ) { _, _ ->

            deletePost(q, collection, parent)

        }//boton s??

        builder.setNegativeButton("No", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()

    }//show alert

    private fun deletePost(question: String, collection: String, parent: String){

        db.collection("Users").document(a).update(collection.lowercase(), FieldValue.increment(-1))

        if(collection == "Questions"){

            deleteAnswers(question)

        }//if

        else {

            db.collection("Questions").document(parent).update("answers", FieldValue.increment(-1))

        }//else

        db.collection(collection).document(question).delete()
        deleteNotifications(question)

        filesFolder = FirebaseStorage.getInstance().reference.child("Files/${question}")
        filesFolder.listAll().addOnCompleteListener {
            it.result!!.items.forEach { file ->
                file.delete()
            }
        }

        Toast.makeText(this, "Publicaci??n eliminada", Toast.LENGTH_SHORT).show()

    }//deletePost

    private fun deleteAnswers(question:String){

        db.collection("Answers").whereEqualTo("question", question).get().addOnSuccessListener {

            it.documents.forEach { i->

                val id = i.get("id") as String

                db.collection("Answers").document(id).delete()
                db.collection("Users").document(i.get("author") as String).update("answers", FieldValue.increment(-1))

            }//for each

        }//obtenemos el id de las perguntas hechas por el usuario

        onBackPressed()

    }//deleteAnswers

    private fun deleteNotifications(value:String){

        db.collection("Notifications").whereEqualTo("question", value).get().addOnSuccessListener {

            it.documents.forEach { i->

                val id = i.get("id") as String

                db.collection("Notifications").document(id).delete()

            }//for each

        }//obtenemos el id de las perguntas hechas por el usuario

    }//deleteAnswers

    private fun reportPost(collection: String) {

        val intent = Intent(this, ReportQuestionActivity::class.java).apply {

            putExtra("id", q)
            putExtra("collection", collection)

        }//homeIntent

        startActivity(intent)

    }//reportPost

    private fun editPost(collection: String) {

        val intent: Intent

        if(collection == "Questions"){

            intent = Intent(this, EditQuestionActivity::class.java).apply {

                putExtra("id", q)

            }//intent

        }//se desea editar la pregunta

        else{

            intent = Intent(this, EditAnswer::class.java).apply {

                putExtra("id", q)
                putExtra("email", user)

            }//intent

        }//en otro caso abrimos el edit question activity

        startActivity(intent)

    }//editPost
    
    private val onMenuItemClickListenerOwner: OnMenuItemClickListener<PowerMenuItem?> =
        OnMenuItemClickListener<PowerMenuItem?> { _, item ->

            if(item.title == "Eliminar"){
                showAlert(collec)
            }//eliminar

            if(item.title == "Editar"){
                editPost(collec)
            }//editar

        }//onMenuItemClickListenerOwner

    private val onMenuItemClickListener: OnMenuItemClickListener<PowerMenuItem?> =
        OnMenuItemClickListener<PowerMenuItem?> { _, item ->

            if(item.title == "Reportar"){
                reportPost(collec)
            }//eliminar

        }//onMenuItemClickListener
    
}//class