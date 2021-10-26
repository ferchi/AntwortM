package edu.itq.antwort.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import edu.itq.antwort.Adapters.AnswerAdapter
import edu.itq.antwort.Adapters.QuestionAdapter
import edu.itq.antwort.Classes.Answers
import edu.itq.antwort.Classes.Questions
import edu.itq.antwort.Classes.Users
import edu.itq.antwort.R
import edu.itq.antwort.Utils
import edu.itq.antwort.databinding.FragmentAnswersBinding
import edu.itq.antwort.databinding.FragmentHomeBinding

class AnswersFragment() : Fragment() {
    private lateinit var binding: FragmentAnswersBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var rev: RecyclerView
    private var answers : MutableList<Answers> = mutableListOf()
    private lateinit var current : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_answers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAnswersBinding.bind(view)
        db = FirebaseFirestore.getInstance()
        rev = binding.rvAnswers
        current = Utils.getEmail(requireActivity()).toString()


        val query = db.collection("Answers").orderBy("date", Query.Direction.DESCENDING).whereEqualTo("author", current)

        query.get().addOnCompleteListener(OnCompleteListener<QuerySnapshot>() {
            answers.addAll(it.result!!.toObjects(Answers::class.java))
            rev.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = AnswerAdapter(this@AnswersFragment,answers)
            }
        })
    }





}