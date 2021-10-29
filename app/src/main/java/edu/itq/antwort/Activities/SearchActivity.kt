package edu.itq.antwort.Activities

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import edu.itq.antwort.Classes.Questions
import edu.itq.antwort.Classes.SearchAdapter
import edu.itq.antwort.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    lateinit var binding: ActivitySearchBinding
    lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        db = FirebaseFirestore.getInstance()
        setup()

        binding.includeToolbarSearch.edtSearchTBS.addTextChangedListener(object : TextWatcher{

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }//beforeTextChanged

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                val searchText = binding.includeToolbarSearch.edtSearchTBS.text.toString().trim()

                if(searchText.length > 2){

                    searchQuestions(searchText)

                }//comenzamos la busqueda a partir de tres letras

                else{

                    searchQuestions("")

                }//eliminamos las busquedas si tiene menos de 3 letras

            }//onTextChanged

            override fun afterTextChanged(p0: Editable?) {

            }//afterTextChanged

        })//addTextChangedListener

    }//OnCreate

    private fun setup(){

        binding.includeToolbarSearch.edtSearchTBS.requestFocus()
        showKeyboard()

        binding.includeToolbarSearch.imgBackTBS.setOnClickListener {

            hideKeyboard()
            onBackPressed()

        }//setOnClickListener

    }//setup

    private fun showKeyboard(){

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

    }//showKeyboard

    private fun hideKeyboard(){

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)

    }//hideKeyboard

    private fun searchQuestions(search: String){

        if(search.isNotEmpty()){

            db.collection("Questions").addSnapshotListener { value, _ ->

                val questions = value!!.toObjects(Questions::class.java)
                val questionsAux: ArrayList<Questions> = ArrayList()

                questions.forEachIndexed { index, question ->

                    question.id = value.documents[index].id

                    val modelQuestion = Questions(question.author, question.name, question.description, question.title, question.id, question.answers, question.date, question.likes, question.dislikes)

                    if(modelQuestion.title.lowercase().contains(search.lowercase())
                        || modelQuestion.description.lowercase().contains(search.lowercase())){

                        questionsAux.add(modelQuestion)

                    }//if

                }//forEachIndexed

                binding.firestoreList.apply {

                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(context)
                    adapter = SearchAdapter(this@SearchActivity, questionsAux)

                }//apply

            }//addSnapshotListener

        }// el texto no esta vacio

        else{

            binding.firestoreList.adapter = null
            binding.firestoreList.layoutManager = LinearLayoutManager(this)

        }//else

    }//searchQuestions

}//class