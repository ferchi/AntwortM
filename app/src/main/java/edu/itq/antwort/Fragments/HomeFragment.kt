package edu.itq.antwort.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.itq.antwort.Activities.EditProfileActivity
import edu.itq.antwort.Activities.SearchActivity
import edu.itq.antwort.Adapters.QuestionAdapter
import edu.itq.antwort.Classes.Questions
import edu.itq.antwort.Methods
import edu.itq.antwort.R
import edu.itq.antwort.databinding.FragmentHomeBinding

class HomeFragment :Fragment(){
    private lateinit var binding: FragmentHomeBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var rev: RecyclerView
    private val currentUser = FirebaseAuth.getInstance().currentUser?.email
    private var questions : MutableList<Questions> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)
        db = FirebaseFirestore.getInstance()
        rev = binding.rvHome

        db.collection("Users").document(currentUser!!).get().addOnSuccessListener {

            val show = (it.get("specialty") as String).isEmpty()

            setup(show)

        }//obtenemos el rol del usuario


        search()
        refreshRecyclerView()
        getData()
    }

    private fun setup(show: Boolean){

        if(show){

            binding.completeProfile.visibility = View.VISIBLE

        }//si los dato no estan actualizados mostramos el mensaje

        binding.completeProfile.setOnClickListener {

            val intent = Intent(context, EditProfileActivity::class.java).apply {

                putExtra("current", currentUser)

            }//apply

            startActivity(intent)

        }//setOnClickListener

    }//setup

    private fun search(){

        binding.includeToolbar.imgSearchTB.setOnClickListener {

            val intent = Intent(context, SearchActivity::class.java)
            startActivity(intent)

        }//setOnClickListener

    }//search

    private fun refreshRecyclerView() {

        binding.refreshHome.setOnRefreshListener {

            getData()
            binding.refreshHome.isRefreshing = false

        }//setOnRefreshListener

    }//refreshRecyclerView

    private fun getData(){
        val query = db.collection("Questions").orderBy("date", Query.Direction.DESCENDING)

        query.get().addOnCompleteListener {
            questions.clear()
            questions.addAll(it.result!!.toObjects(Questions::class.java))
            rev.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = QuestionAdapter(this@HomeFragment, questions)
            }
        }
    }
}