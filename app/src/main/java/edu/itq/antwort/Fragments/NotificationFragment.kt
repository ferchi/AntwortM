package edu.itq.antwort.Fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.itq.antwort.Activities.SearchActivity
import edu.itq.antwort.Adapters.NotificationAdapter
import edu.itq.antwort.Classes.Notifications
import edu.itq.antwort.Methods
import edu.itq.antwort.R
import edu.itq.antwort.databinding.FragmentNotificationBinding


class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private val db = FirebaseFirestore.getInstance()
    private var notifications : MutableList<Notifications> = mutableListOf()
    private lateinit var rev: RecyclerView
    private val currentUser = FirebaseAuth.getInstance().currentUser?.email

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        return inflater.inflate(R.layout.fragment_notification, container, false)

    }//onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding = FragmentNotificationBinding.bind(view)
        rev = binding.rvNotifications

        binding.includeToolbar.imgSettingsTB.setOnClickListener {

            startActivity(Intent(context, SearchActivity::class.java))

        }//setOnClickListener

        getData()
        refreshRecyclerView()

    }//onViewCreated

    private fun refreshRecyclerView() {

        binding.refreshNotifications.setOnRefreshListener {

            getData()
            binding.refreshNotifications.isRefreshing = false

        }//setOnRefreshListener

    }//refreshRecyclerView

/*
    private fun showAlert(){

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Eliminar notificaciones")
        builder.setMessage("¿Esta seguro que desea elimiar todas las notificaciones?")
        builder.setPositiveButton("Sí"
        ) { _, _ ->

            deleteAllNotifications()
        }
        builder.setNegativeButton("No", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()

    }//show alert

    private fun deleteAllNotifications(){

        db.collection("Notifications").whereEqualTo("user", currentUser).get().addOnSuccessListener {

            it.documents.forEach {item ->

                db.collection("Notifications").document(item.get("id") as String).delete()
                //it.documents.removeAt(item.)

            }//forEach

        }//obtenemos las notificaciones del usuario

    }//delete
*/
    private fun getData(){

        val query = db.collection("Notifications").orderBy("date", Query.Direction.DESCENDING).whereEqualTo("user" ,
            Methods.getEmail(requireActivity())
        )
        query.get().addOnCompleteListener {
            notifications.clear()
            notifications.addAll(it.result!!.toObjects(Notifications::class.java))
            rev.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = NotificationAdapter(this@NotificationFragment, notifications)
            }
        }
    }

}//fragment