package edu.itq.antwort.Adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import edu.itq.antwort.Classes.NotificationData
import edu.itq.antwort.Classes.PushNotification
import edu.itq.antwort.Classes.Questions
import edu.itq.antwort.Classes.RetrofitInstance
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ItemQuestionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import android.view.Gravity
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.PowerMenuItem
import com.skydoves.powermenu.PowerMenu
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FieldValue
import com.skydoves.powermenu.OnMenuItemClickListener
import edu.itq.antwort.Activities.*
import edu.itq.antwort.Methods

class QuestionAdapter (private val fragment: Fragment, private val dataset: MutableList<Questions>):
    RecyclerView.Adapter<QuestionAdapter.ViewHolder>() {

    class ViewHolder (val binding: ItemQuestionBinding) : RecyclerView.ViewHolder(binding.root)

    private val db = FirebaseFirestore.getInstance()
    private lateinit var popUpMenu :  PowerMenu.Builder
    private var q: String = ""
    private var a: String = ""
    private var questionPosition: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemQuestionBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount() = dataset.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val question = dataset[position]
        popUpMenu = PowerMenu.Builder(fragment.requireContext())
        holder.binding.questionOptions.setOnClickListener {

            popUpMenu.build().clearPreference()

            if(getEmail() == question.author){

                createPopUpOwner()
                questionPosition = holder.layoutPosition

            }//es su pregunta

            else{

                createPopUp()

            }//no es su pregunta

            popUpMenu.build().showAsDropDown(it)
            q = question.id
            a = question.author

        }
        if(Methods.getEmail(fragment.requireActivity()).toString() != (question.author)) {
            holder.binding.imgAuthorAI.setOnClickListener {

                val homeIntent =
                    Intent(fragment.requireContext(), ProfileActivity::class.java).apply {

                        putExtra("email", question.author)

                    }//homeIntent

                fragment.startActivity(homeIntent)
            }
        }

        holder.binding.cvSI.setOnClickListener {
            val homeIntent = Intent(fragment.requireContext(), QuestionDetails::class.java).apply {

                putExtra("id", question.id)

            }//homeIntent
            fragment.startActivity(homeIntent)
        }


        reactions(question, question.likes, question.dislikes, holder.binding.likesIA, "Útil", question.author, question.title, question.id)
        reactions(question, question.dislikes, question.likes, holder.binding.dislikesIQ, "No útil", question.author, question.title, question.id)

        holder.binding.answersIQ.setOnClickListener {
            val answerIntent = Intent(fragment.requireContext(), AnswerScreenActivity::class.java).apply {

                putExtra("email", getEmail())
                putExtra("author", question.author)
                putExtra("authorName", question.name)
                putExtra("question", question.id)

            }//answerIntent
            fragment.startActivity(answerIntent)
        }

        if(question.answers >0)
            holder.binding.answersIQ.text = question.answers.toString()
        else
            holder.binding.answersIQ.text = ""

        holder.binding.txtTitleText.text = question.title
        holder.binding.txtItemDescription.text = question.description
        holder.binding.txtAuthor.text = question.name
        loadImg(holder.binding.imgAuthorAI, question.author)

        if ((holder.binding.chipGroupItemQuestion.childCount) == 0){
            question.topics.forEach {
                addTag(it, holder)
            }
        }

    }

    private fun loadImg(image : CircleImageView, author: String) {

        db.collection("Users").document(author).addSnapshotListener{
                result, _ ->
            val urlImg = result!!.get("imgProfile").toString()

            try {
                if(urlImg.isNotEmpty())
                    Picasso.get().load(urlImg).into(image)

            } catch (e: Exception) {
                Picasso.get().load(R.drawable.ic_user_profile).into(image)
            }
        }
    }//load image

    @SuppressLint("InflateParams")
    private fun addTag(s: CharSequence, holder: ViewHolder) {
        val layoutInflater = LayoutInflater.from(fragment.requireContext())
        val tag = layoutInflater.inflate(R.layout.item_topic_show, null, false) as Chip
        tag.text = s

        holder.binding.chipGroupItemQuestion.addView(tag)
    }

    private fun reactions(model: Questions, mainArray: ArrayList<String>, secondArray: ArrayList<String>, txtReaction: TextView, text: String, author: String, title: String, id:String){

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

    @SuppressLint("NotifyDataSetChanged")
    private fun updateQuestion(model: Questions){

        db.collection("Questions").document(model.id).update("likes", model.likes)
        db.collection("Questions").document(model.id).update("dislikes", model.dislikes)

        this.notifyDataSetChanged()

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

        db.collection("Users").document(email).get().addOnSuccessListener { it ->

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

    private fun createPopUpOwner(){

        val context = fragment.requireContext()
        popUpMenu = PowerMenu.Builder(fragment.requireContext())
        popUpMenu
            .addItem(PowerMenuItem("Editar", false))
            .addItem(PowerMenuItem("Eliminar", false))
            .setAnimation(MenuAnimation.FADE) // Animation start point (TOP | LEFT).
            .setMenuRadius(10f) // sets the corner radius.
            .setMenuShadow(10f) // sets the shadow.
            .setTextColor(ContextCompat.getColor(context, R.color.black))
            .setTextGravity(Gravity.CENTER)
            //.setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            .setSelectedTextColor(Color.WHITE)
            .setMenuColor(Color.WHITE)
            .setSelectedMenuColor(ContextCompat.getColor(context, R.color.orange))
            .setOnMenuItemClickListener(onMenuItemClickListenerOwner)
            .setAutoDismiss(true)
            .build()

    }//createPopUpOwner

    private fun createPopUp(){

        val context = fragment.requireContext()
        popUpMenu = PowerMenu.Builder(fragment.requireContext())
        popUpMenu
            .addItem(PowerMenuItem("Reportar", false))
            .setAnimation(MenuAnimation.FADE) // Animation start point (TOP | LEFT).
            .setMenuRadius(10f) // sets the corner radius.
            .setMenuShadow(10f) // sets the shadow.
            .setTextColor(ContextCompat.getColor(context, R.color.black))
            .setTextGravity(Gravity.CENTER)
            //.setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            .setSelectedTextColor(Color.WHITE)
            .setMenuColor(Color.WHITE)
            .setSelectedMenuColor(ContextCompat.getColor(context, R.color.orange))
            .setOnMenuItemClickListener(onMenuItemClickListener)
            .setAutoDismiss(true)
            .build()
    }

    private fun showAlert(){

        val builder = AlertDialog.Builder(fragment.requireContext())
        builder.setTitle("Eliminar publicación")
        builder.setMessage("¿Esta seguro que desea elimiar la publicación?")
        builder.setPositiveButton("Sí"
        ) { _, _ ->
            println("Question show alert si: $q")
            deleteQuestion(q)
        }
        builder.setNegativeButton("No", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()

    }//show alert

    private fun deleteQuestion(question: String){

        db.collection("Questions").document(question).delete()
        db.collection("Users").document(a).update("questions", FieldValue.increment(-1))
        deleteAnswers(question)
        deleteNotifications(question)
        Toast.makeText(fragment.requireContext(), "Publicación eliminada", Toast.LENGTH_SHORT).show()

        dataset.removeAt(questionPosition)
        this.notifyItemRemoved(questionPosition)


    }//deleteQuestion

    private fun deleteAnswers(question:String){

        db.collection("Answers").whereEqualTo("question", question).get().addOnSuccessListener {

            it.documents.forEach { i->

                val id = i.get("id") as String

                db.collection("Answers").document(id).delete()
                db.collection("Users").document(i.get("author") as String).update("answers", FieldValue.increment(-1))

            }//for each

        }//obtenemos el id de las perguntas hechas por el usuario

    }//deleteAnswers

    private fun deleteNotifications(question:String){

        db.collection("Notifications").whereEqualTo("question", question).get().addOnSuccessListener {

            it.documents.forEach { i->

                val id = i.get("id") as String

                db.collection("Notifications").document(id).delete()

            }//for each

        }//obtenemos el id de las perguntas hechas por el usuario

    }//deleteAnswers

    private fun reportQuestion() {

        val intent = Intent(fragment.requireContext(), ReportQuestionActivity::class.java).apply {

            putExtra("id", q)
            putExtra("collection", "Questions")

        }//homeIntent

        fragment.startActivity(intent)

    }//reportQuestion

    private fun editQuestion() {

        val intent = Intent(fragment.requireContext(), EditQuestionActivity::class.java).apply {

            putExtra("id", q)

        }//homeIntent

        fragment.startActivity(intent)

    }//editQuestion

    private val onMenuItemClickListenerOwner: OnMenuItemClickListener<PowerMenuItem?> =
        OnMenuItemClickListener<PowerMenuItem?> { _, item ->

            if(item.title == "Eliminar"){
                showAlert()
            }//eliminar

            if(item.title == "Editar"){
                editQuestion()
            }//editar

        }//onMenuItemClickListenerOwner

    private val onMenuItemClickListener: OnMenuItemClickListener<PowerMenuItem?> =
        OnMenuItemClickListener<PowerMenuItem?> { _, item ->

            if(item.title == "Reportar"){
                reportQuestion()
            }//eliminar

        }//onMenuItemClickListener

}//class home fragment