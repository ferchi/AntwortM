package edu.itq.antwort.Activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ActivityAlertsBinding

class AlertsActivity : AppCompatActivity() {

    lateinit var binding: ActivityAlertsBinding
    private var topicsList : MutableList<CharSequence> = mutableListOf()
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser?.email

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityAlertsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        db.collection("Users").document(currentUser!!).get().addOnSuccessListener {

            setup(it.get("topics") as ArrayList<String>)

        }//obtenemos la lista de topicos a los que esta suscrito

    }//onCreate

    private fun setup(topics: ArrayList<String>){

        binding.edtNewAlert.requestFocus()

        topics.forEach {

            Firebase.messaging.unsubscribeFromTopic("/topics/$it")

        }//forEach desuscribimos de los topics

        if(binding.chipGroupNewAlert.childCount == 0 && topics.isNotEmpty()){

            topics.forEach {

                addTag(it)

            }//forEach

        }//no hay elementos el el chip group

        binding.imgBackAlerts.setOnClickListener {

            //Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
            db.collection("Users").document(currentUser!!).update("topics", topicsList)

            topicsList.forEach {

                Firebase.messaging.subscribeToTopic("/topics/$it")

            }//susbribimos a los topicos que indico el usuario

            onBackPressed()

        }//se presiono el boton de regresar

        binding.btnSaveTopics.setOnClickListener {

            Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
            db.collection("Users").document(currentUser!!).update("topics", topicsList)

            topicsList.forEach {

                Firebase.messaging.subscribeToTopic("/topics/$it")

            }//susbribimos a los topicos que indico el usuario

            onBackPressed()

        }//se presiono el boton de regresar

        binding.edtNewAlert.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int ) {

            }
            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if(s.endsWith(' ') && (s.isNotBlank())){
                    addTag(s)
                    binding.edtNewAlert.text.clear()
                }
            }
        })

        binding.edtNewAlert.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                // if the event is a key down event on the enter button
                if (event.action == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    addTag(binding.edtNewAlert.text)
                    binding.edtNewAlert.text.clear()
                    return true
                }
                return false
            }
        })

    }//setup

    @SuppressLint("InflateParams")
    private fun addTag(s: CharSequence) {

        val tagText = s.toString().lowercase().filter { !it.isWhitespace() }
        val layoutInflater = LayoutInflater.from(baseContext)
        val tag = layoutInflater.inflate(R.layout.item_topic_create, null, false) as Chip
        tag.text = tagText

        tag.setOnCloseIconClickListener {

            val text = (it as Chip).text
            topicsList.remove(text)
            binding.chipGroupNewAlert.removeView(it)

        }//setOnCloseIconClickListener

        if(tagText !in topicsList){

            topicsList.add(tagText)
            binding.chipGroupNewAlert.addView(tag)

        }//if

    }//addtag

}//class