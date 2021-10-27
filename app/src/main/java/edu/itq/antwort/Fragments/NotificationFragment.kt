package edu.itq.antwort.Fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import edu.itq.antwort.Activities.QuestionDetails
import edu.itq.antwort.Classes.Notifications
import edu.itq.antwort.R
import edu.itq.antwort.databinding.FragmentNotificationBinding
import edu.itq.antwort.databinding.ItemNotificationBinding

class NotificationsViewHolder(val notificationBinding: ItemNotificationBinding) : RecyclerView.ViewHolder(notificationBinding.root)

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var db : FirebaseFirestore

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        return inflater.inflate(R.layout.fragment_notification, container, false)

    }//onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding = FragmentNotificationBinding.bind(view)
        showNotifications()

    }//onViewCreated

    private fun showNotifications(){

        db = FirebaseFirestore.getInstance()
        val query = db.collection("Notifications").whereEqualTo("user" , getEmail())

        val options = FirestoreRecyclerOptions.Builder<Notifications>().setQuery(query, Notifications::class.java).setLifecycleOwner(this).build()

        val adapter = object: FirestoreRecyclerAdapter<Notifications, NotificationsViewHolder>(options){

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsViewHolder {

                return NotificationsViewHolder(ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

            }//onCreateViewHolder

            override fun onBindViewHolder(holder: NotificationsViewHolder, position: Int, model: Notifications) {

                holder.notificationBinding.cvNotification.setOnClickListener {

                    val homeIntent = Intent(context, QuestionDetails::class.java).apply {

                        putExtra("id", model.question)

                    }//homeIntent

                    startActivity(homeIntent)

                }//setOnClickListener

                val title: TextView = holder.itemView.findViewById(R.id.txtTitleIN)
                val txtContentNI: TextView = holder.itemView.findViewById(R.id.txtContentNI)

                title.text = model.title
                txtContentNI.text = model.content

            }//onBindViewHolder

        }//adapter

        binding.rvNotifications.adapter = adapter
        binding.rvNotifications.layoutManager = LinearLayoutManager(context)

    }//showNotifications

    private fun getEmail() : String?{

        val prefs : SharedPreferences =  requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        return prefs.getString("email", null)

    }//getMail

}//fragment