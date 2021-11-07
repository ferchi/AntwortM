package edu.itq.antwort.Activities

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.firestore.FirebaseFirestore
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ActivityReportQuestionBinding

class ReportQuestionActivity : AppCompatActivity() {

    lateinit var binding: ActivityReportQuestionBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityReportQuestionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bundle = intent.extras
        val id = bundle?.getString("id")
        val collection = bundle?.getString("collection")

        db.collection(collection!!).document(id!!).get().addOnSuccessListener {

            setup(id!!, it.get("author") as String, collection)

        }//obtenemos el correo del autor de la pregunta reportada

    }//onCreate

    private fun setup(document: String, author: String, collection: String){

        val arrayAdapter:ArrayAdapter<String>
        val options = mutableListOf("Es sospechoso o spam", "Comete abusos o es perjudicial", "Lenguaje que incita al odio", "Acoso", "Violencia", "Otro")

        arrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1, options)
        binding.lvReport.adapter = arrayAdapter

        binding.lvReport.setOnItemClickListener{parent, _, position, _ ->

            getReason(document, author, collection, parent.getItemAtPosition(position).toString())

        }//setOnClickListener

        binding.imgBackRQ.setOnClickListener {

            onBackPressed()

        }//setOnClickListener

    }//setup

    private fun generateReport(document: String, author: String, collection: String, reason: String){

        val id: String = db.collection("Reports").document().id

        db.collection("Reports").document(id).set(

            hashMapOf(

                "id" to id,
                "collection" to collection,
                "document" to document,
                "author" to author,
                "reason" to reason

            )//hashMap

        )//creamo el nuevo reporte

        Toast.makeText(this, "Atenderemos tu reporte lo más pronto posible", Toast.LENGTH_SHORT).show()
        onBackPressed()

    }//generate report

    private fun getReason(document: String, author: String, collection: String, reason: String){

        if(reason == "Otro"){

            val draw = ResourcesCompat.getDrawable(resources, R.drawable.edt_report, null)
            val input = EditText(this)
            input.background = draw
            input.requestFocus()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Cuentanos que ocurre")
            builder.setMessage("Por favor, indicanos cual es el problema con la publicación")
            builder.setView(input)
            builder.setPositiveButton("Enviar"
            ) { _, _ ->

                generateReport(document, author, collection, input.text.toString())

            }//se envio el reporte
            builder.setNegativeButton("Cancelar", null)
            val dialog: AlertDialog = builder.create()
            dialog.show()

        }//Si se eligio "Otro" solicitamos la razon

        else{

            generateReport(document, author, collection, reason)

        }//else

    }//getReason

}//class