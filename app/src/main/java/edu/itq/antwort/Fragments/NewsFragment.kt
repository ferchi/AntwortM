package edu.itq.antwort.Fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.itq.antwort.Activities.AlertsActivity
import edu.itq.antwort.Adapters.QuestionAdapter
import edu.itq.antwort.Classes.Questions
import edu.itq.antwort.R
import edu.itq.antwort.databinding.FragmentNewsBinding

class NewsFragment : Fragment() {

    private lateinit var binding: FragmentNewsBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var rev: RecyclerView
    private val currentUser = FirebaseAuth.getInstance().currentUser?.email
    private var questions : MutableList<Questions> = mutableListOf()
    private lateinit var questionAdapter: QuestionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }//onCreate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNewsBinding.bind(view)
        db = FirebaseFirestore.getInstance()
        rev = binding.rvAlerts

        setup()

        binding.swLayoutContainer.setOnRefreshListener {
            questions.clear()
            getData()
            binding.swLayoutContainer.isRefreshing = false
        }
        getData()

    }//onViewCreate



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news, container, false)
    }//onCreateView

    private fun setup(){

        binding.btnOptionsNews.setOnClickListener {

            val intent = Intent(context, AlertsActivity::class.java)
            startActivity(intent)

        }//se le dio click a administrar alertas

    }//private fun

    private fun getData(){

        db.collection("Users").document(currentUser!!).get().addOnSuccessListener {


            val topics = (it.get("topics") as ArrayList<String>)
            if(topics.size > 0) {
                topics.forEach { topic ->

                    val query =
                        db.collection("Questions").orderBy("date", Query.Direction.DESCENDING)
                            .whereArrayContains("topics", topic)
                    query.get().addOnCompleteListener {

                        questions.addAll(it.result!!.toObjects(Questions::class.java))

                        if (topics.indexOf(topic) == topics.lastIndex) {
                            questionAdapter = QuestionAdapter(this@NewsFragment, questions)

                            rev.apply {
                                setHasFixedSize(true)
                                layoutManager = LinearLayoutManager(context)
                                adapter = questionAdapter
                            }
                        }//if
                    }
                }//forEach
            } else {
                questionAdapter = QuestionAdapter(this@NewsFragment, questions)
                rev.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(context)
                    adapter = questionAdapter
                }
            }
        }//obtener los topicos a los que esta suscrito el usuario
    }//getData

}//class