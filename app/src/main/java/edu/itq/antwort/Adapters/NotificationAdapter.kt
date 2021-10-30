package edu.itq.antwort.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import edu.itq.antwort.Activities.QuestionDetails
import edu.itq.antwort.Classes.Notifications
import edu.itq.antwort.databinding.ItemNotificationBinding

class NotificationAdapter (private val fragment: Fragment, private val notifications: List<Notifications>):
RecyclerView.Adapter<NotificationAdapter.ViewHolder>()
{
    class ViewHolder(val binding: ItemNotificationBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemNotificationBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount() = notifications.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.cvNotification.setOnClickListener {
            val homeIntent = Intent(fragment.requireContext(), QuestionDetails::class.java).apply {
                putExtra("id", notifications[position].question)
            }//homeIntent
            fragment.startActivity(homeIntent)
        }//setOnClickListener
        holder.binding.txtTitleIN.text = notifications[position].title
        holder.binding.txtContentNI.text = notifications[position].content
    }

}