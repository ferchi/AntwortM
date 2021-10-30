package edu.itq.antwort.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.itq.antwort.Adapters.QuestionAdapter
import edu.itq.antwort.Classes.Questions
import edu.itq.antwort.R
import edu.itq.antwort.databinding.FragmentHomeBinding

class HomeFragment :Fragment(){
    private lateinit var binding: FragmentHomeBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var rev: RecyclerView
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

        getData()
    }


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