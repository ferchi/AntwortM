package edu.itq.antwort.Adapters

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import edu.itq.antwort.Activities.*
import edu.itq.antwort.Classes.*
import edu.itq.antwort.Methods
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ItemAnswerViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnswerAdapter (private val fragment: Fragment, private val dataset: List<Answers>):
    RecyclerView.Adapter<AnswerAdapter.ViewHolder>() {

    class ViewHolder (val binding: ItemAnswerViewBinding) : RecyclerView.ViewHolder(binding.root)

    private val db = FirebaseFirestore.getInstance()
    private var popUpMenu = PowerMenu.Builder(fragment.requireContext())
    private var ans: String = ""
    private var que: String = ""
    private var a: String = ""
    
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

        if(answer.edited){
            holder.binding.ivItemAnswerEdit.visibility = View.VISIBLE
        }


        if(Methods.getEmail(fragment.requireActivity()).toString() != (answer.author)) {
            holder.binding.imgUserAV.setOnClickListener {

                val homeIntent =
                    Intent(fragment.requireContext(), ProfileActivity::class.java).apply {

                        putExtra("email", answer.author)

                    }//homeIntent

                fragment.startActivity(homeIntent)
            }
        }

        reactions(answer, answer.likes, answer.dislikes, holder.binding.likesIA, "Útil", user!!, answer.author, answer.id, answer.question, position)
        reactions(answer, answer.dislikes, answer.likes, holder.binding.dislikeIA, "No útil", user, answer.author, answer.id, answer.question, position)


        holder.binding.txtNameAV.text = answer.nameAuthor
        holder.binding.txtAnswersAV.text = answer.content
        loadImg(holder.binding.imgUserAV, answer.author)

        holder.binding.imgOptionAV.setOnClickListener {

            popUpMenu.build().clearPreference()
            
            createPopUpOwner()
            
            popUpMenu.build().showAsDropDown(it)
            ans = answer.id
            a = answer.author
            que = answer.question
            
        }//se presiono opciones
        
        holder.binding.cardItemAnswer.setOnClickListener {

            val homeIntent = Intent(fragment.requireContext(), QuestionDetails::class.java).apply {

                putExtra("id", answer.question)

            }//homeIntent

            fragment.startActivity(homeIntent)

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

    private fun reactions(modelAnswers: Answers?, mainArray: ArrayList<String>, secondArray: ArrayList<String>, txtReaction: TextView, text: String, user: String, author: String, id: String, question: String, position: Int){

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

                    db.collection("Answers").document(id).get().addOnSuccessListener {

                        createNotification(it.get("content") as String, user, question, author)
                        showNotification(it.get("content") as String, question, author)

                    }//obtenemos el contenido de la respuesta

                }//creamos notificacion solo si no estoy dandome like a mi mismo

                mainArray.add(user)

            }//mi correo no esta en la lista

            if(modelAnswers != null){

                updateAnswers(modelAnswers,position)

            }//el modelo de respuestas no es nulo

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

    private fun showNotification(message: String, question: String, email:String){

        db.collection("Users").document(email).get().addOnSuccessListener { it ->

            val recipientToken = it.get("token") as String?

            if(message.isNotEmpty()){

                PushNotification(

                    NotificationData("Han reaccionado a tu respuesta'", message, question, email),
                    recipientToken?:""

                ).also {

                    sendNotification(it)

                }//also

            }//obtener token del usuario

        }//obtenemos los datos de quien dio like

    }//show Notification

    private fun createNotification(content: String, user:String, question: String, author: String) {

        val id: String = db.collection("Notifications").document().id

        db.collection("Notifications").document(id).set(

            hashMapOf(

                "id" to id,
                "title" to "Han reaccionado a tu respuesta'",
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
                "content" to model.content,
                "question" to model.question,
                "likes" to model.likes,
                "dislikes" to model.dislikes

            )//hashMapOf con los nuevos datos

        )//actualizamos el numero de likes

        this.notifyItemChanged(position)

    }//update answers

    private fun createPopUpOwner(){

        val context = fragment.requireContext()
        
        popUpMenu = PowerMenu.Builder(context)
        popUpMenu
            .addItem(PowerMenuItem("Editar", false))
            .addItem(PowerMenuItem("Eliminar", false))
            .setAnimation(MenuAnimation.FADE) // Animation start point (TOP | LEFT).
            .setMenuRadius(10f) // sets the corner radius.
            .setMenuShadow(10f) // sets the shadow.
            .setTextColor(ContextCompat.getColor(context, R.color.black))
            .setTextGravity(Gravity.CENTER)
            .setSelectedTextColor(Color.WHITE)
            .setMenuColor(Color.WHITE)
            .setSelectedMenuColor(ContextCompat.getColor(context, R.color.orange))
            .setOnMenuItemClickListener(onMenuItemClickListenerOwner)
            .setAutoDismiss(true)
            .build()

    }//createPopUpOwner

    private fun showAlert(){

        val builder = AlertDialog.Builder(fragment.requireContext())
        builder.setTitle("Eliminar publicación")
        builder.setMessage("¿Esta seguro que desea elimiar la publicación?")
        builder.setPositiveButton("Sí"
        ) { _, _ ->
            deleteAnswer(ans)
        }
        builder.setNegativeButton("No", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()

    }//show alert

    private fun deleteAnswer(answer: String){

        db.collection("Answers").document(answer).delete()
        db.collection("Users").document(a).update("answers", FieldValue.increment(-1))
        db.collection("Questions").document(que).update("answers", FieldValue.increment(-1))
        Toast.makeText(fragment.requireContext(), "Publicación eliminada", Toast.LENGTH_SHORT).show()

    }//deleteAnswer

    private fun editAnswer() {

        val intent = Intent(fragment.requireContext(), EditAnswer::class.java).apply {

            putExtra("id", ans)
            putExtra("email", Methods.getEmail(fragment.requireActivity()))

        }//homeIntent

        fragment.startActivity(intent)

    }//editAnswer
    
    private val onMenuItemClickListenerOwner: OnMenuItemClickListener<PowerMenuItem?> =
        OnMenuItemClickListener<PowerMenuItem?> { _, item ->

            if(item.title == "Eliminar"){
                showAlert()
            }//eliminar

            if(item.title == "Editar"){
                editAnswer()
            }//editar

        }//onMenuItemClickListenerOwner
    
}//class