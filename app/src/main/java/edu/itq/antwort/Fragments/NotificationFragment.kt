package edu.itq.antwort.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import edu.itq.antwort.Adapters.NotificationAdapter
import edu.itq.antwort.Classes.Notifications
import edu.itq.antwort.Methods
import edu.itq.antwort.R
import edu.itq.antwort.databinding.FragmentNotificationBinding


class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var db : FirebaseFirestore
    private var notifications : MutableList<Notifications> = mutableListOf()
    private lateinit var rev: RecyclerView

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
        db = FirebaseFirestore.getInstance()

        getData()
    }//onViewCreated

    private fun getData(){
        val query = db.collection("Notifications").whereEqualTo("user" ,
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