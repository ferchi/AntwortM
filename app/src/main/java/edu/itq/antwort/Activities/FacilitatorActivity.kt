package edu.itq.antwort.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import edu.itq.antwort.Methods.sendEmail
import edu.itq.antwort.databinding.ActivityAlertsBinding
import edu.itq.antwort.databinding.ActivityFacilitatorBinding

class FacilitatorActivity : AppCompatActivity() {

    lateinit var binding: ActivityFacilitatorBinding
    private val currentUser = FirebaseAuth.getInstance().currentUser?.email

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityFacilitatorBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setup()

    }//onCreate

    private fun setup(){

        binding.imgBackFacilitator.setOnClickListener{onBackPressed()}

        binding.requestRol.setOnClickListener{

            if(binding.editTextReasons.text.isNotEmpty()){

                val body = "¡Hola administrador! <br> " +
                        "El usuario $currentUser solicita obtener el rol de facilitador<br><br>" +
                        "Sus motivos son: ${binding.editTextReasons.text}.<br><br>"+
                        "Tu decides si se lo otorgas. ;) <br><br>"+
                        "Saludos."

                sendEmail("ramirezguillermo19@gmail.com", "Solicitud de rol",body, this)
                sendEmail("fsalinas628@gmail.com", "Solicitud de rol",body, this)

                onBackPressed()

            }//el texto de motivos no esta vacio

            else{

                Toast.makeText(this, "No olvide rellenar el campo de motivos", Toast.LENGTH_SHORT).show()

            }//else el cuadro de motivos esta vacio

        }//setOnClickListener

    }//setup

}//class