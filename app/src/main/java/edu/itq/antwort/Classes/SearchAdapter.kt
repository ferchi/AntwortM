package edu.itq.antwort.Classes

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import edu.itq.antwort.Activities.QuestionDetails
import edu.itq.antwort.R
import edu.itq.antwort.glass.GlassView

class SearchAdapter(private val activity: AppCompatActivity, private val newList: ArrayList<Questions>) :
    RecyclerView.Adapter<SearchAdapter.SearchAdapterHolder>() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchAdapterHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_question, parent, false)
        return SearchAdapterHolder(itemView)
    }//onCreateViewHolder

    override fun onBindViewHolder(holder: SearchAdapterHolder, position: Int) {

        val currentItem = newList[position]
        holder.titleSA.text = currentItem.title
        holder.descriptionSA.text = currentItem.description
        holder.authorSA.text = currentItem.name
        holder.interactions.visibility = View.GONE
        holder.questionOptions.visibility = View.GONE

        if(holder.chipGroup.childCount == 0){

            currentItem.topics.forEach {

                addTag(it, holder.chipGroup)

            }//agregamos las tags al chipGroup

        }//if

        loadImg(holder.imgAuthorAI, currentItem.author)

        holder.card.setOnClickListener {

            val intent = Intent(activity, QuestionDetails::class.java).apply {

                putExtra("id", currentItem.id)

            }//apply

            activity.startActivity(intent)
            hideKeyboard(activity)

        }//setOnClickListener

    }//onBindViewHolder

    override fun getItemCount(): Int {

        return newList.size

    }//getItemCount

    class SearchAdapterHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        val titleSA : TextView = itemView.findViewById(R.id.txtTitleText)
        val descriptionSA : TextView = itemView.findViewById(R.id.txtItemDescription)
        val authorSA : TextView = itemView.findViewById(R.id.txtAuthor)
        val interactions : ConstraintLayout = itemView.findViewById(R.id.interactions)
        val card : GlassView = itemView.findViewById(R.id.item_question_card)
        val imgAuthorAI : CircleImageView = itemView.findViewById(R.id.imgAuthorAI)
        val questionOptions : ImageView = itemView.findViewById(R.id.questionOptions)
        val chipGroup : com.google.android.material.chip.ChipGroup = itemView.findViewById(R.id.chip_group_item_question)

    }//SearchAdapterHolder

    @SuppressLint("InflateParams")
    private fun addTag(s: CharSequence, holder: com.google.android.material.chip.ChipGroup) {
        
        val layoutInflater = LayoutInflater.from(activity)
        val tag = layoutInflater.inflate(R.layout.item_topic_show, null, false) as Chip
        tag.text = s

        holder.addView(tag)

    }//agregamos las tags a las preguntas

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
    }

    private fun hideKeyboard(context: AppCompatActivity){

        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(context.currentFocus!!.windowToken, 0)

    }//hideKeyboard

}//class