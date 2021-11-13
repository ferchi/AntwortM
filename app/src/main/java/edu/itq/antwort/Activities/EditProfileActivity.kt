package edu.itq.antwort.Activities

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import edu.itq.antwort.Classes.Users
import edu.itq.antwort.Methods
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ActivityEditProfileBinding
import okhttp3.internal.Util
import java.io.ByteArrayOutputStream

class EditProfileActivity : AppCompatActivity() {
    private var db = FirebaseFirestore.getInstance()
    lateinit var binding: ActivityEditProfileBinding

    // Variables para obtener y cambiar de las imagenes de perfil
    val TAKE_IMG_CODE = 1046
    lateinit var vista: View
    lateinit var storageChild: String
    lateinit var databaseChild: String
    private val currentUser = FirebaseAuth.getInstance().currentUser?.email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        Log.d("name", Methods.getEmail(this)!!)
        getName()
        binding.etEditUsername.requestFocus()

        db.collection("Users").document(currentUser!!).get().addOnSuccessListener {

            if(it.get("specialty") as String == "")
                hideAll()

            when ((it.get("rol") as String)) {

                "estudiante" -> setupSpecialty(true)
                "docente" -> setupSpecialty(false)
                "facilitador" -> setupSpecialty(false)

            }//when

        }//addOnSuccessListener

        binding.civEditPhoto.setOnClickListener {
            changeImg()
        }

        binding.imgBackEditProfile.setOnClickListener {

            onBackPressed()

        }//se presiono regresar

        binding.btnEditUsername.setOnClickListener {

            if(binding.etEditUsername.text.toString().isNotEmpty()){

                db.collection("Users").document(currentUser!!).update("name", binding.etEditUsername.text.toString())
                updateAnswers(Methods.getEmail(this)!!, binding.etEditUsername.text.toString())
                updateQuestions(Methods.getEmail(this)!!, binding.etEditUsername.text.toString())
                Methods.customToast(this,"Actualizado")
                onBackPressed()

            }//el nuevo nombre no esta vacio

            else{

                Methods.customToast(this, "No deje el nombre vacio")

            }//el nuevo nombre esta vacio

        }
        loadImg()

    }

    private fun hideAll() {

        binding.civEditPhoto.visibility = View.GONE
        binding.txtChanguePhoto.visibility = View.GONE
        binding.linearLayoutName.visibility = View.GONE
        binding.btnEditUsername.visibility = View.GONE

        binding.txtSelectSpecialty.visibility = View.VISIBLE
        binding.listSpecialty.visibility = View.VISIBLE
        binding.btnSaveSpecialty.visibility = View.VISIBLE

        binding.titleEditProfile.text = "Completar perfil"

    }//hideAll

    private fun updateAnswers(email:String, name: String){

        db.collection("Answers").whereEqualTo("author", email).get().addOnSuccessListener {

            it.documents.forEach { i->

                val id = i.get("id") as String

                db.collection("Answers").document(id).update("nameAuthor", name)

            }//for each

        }//obtenemos el id de las perguntas hechas por el usuario

    }//updateAnswers

    private fun updateQuestions(email:String, name: String){

        db.collection("Questions").whereEqualTo("author", email).get().addOnSuccessListener {

            it.documents.forEach { i->

                val id = i.get("id") as String

                db.collection("Questions").document(id).update("name", name)

            }//for each

        }//obtenemos el id de las perguntas hechas por el usuario

    }//updateQuestions

    private fun getName(){

        val queryRol = db.collection("Users").document(Methods.getEmail(this)!!)
        queryRol.get().addOnCompleteListener {
            val name = it.result!!.toObject(Users::class.java)!!.name

            binding.etEditUsername.setText(name)
        }
    }
    private fun changeImg(): Boolean {

        vista = binding.civEditPhoto
        databaseChild = "imgProfile"
        storageChild = "profileImages"

        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"

        if (intent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(intent, TAKE_IMG_CODE)
        }

        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TAKE_IMG_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val bitmap: Bitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, data?.data)
                    
                    binding.civEditPhoto.setImageBitmap(bitmap)

                    handleUpload(bitmap)
                }
            }
        }
    }

    private fun setupSpecialty(isStudent : Boolean){

        val students = mutableListOf("Arquitectura", "Ing. En Eléctrica", "Ing. En Electrónica", "Ing. En Gestión Empresarial", "Ing. Industrial", "Ing. En Logística", "Ing. En Materiales", "Ing. En Mecánica", "Ing. En Mecatrónica", "Ing. En Sistemas Computacionales")
        val teachers = mutableListOf("Desarrollo académico", "Ciencias básicas", "Ciencias económico administrativas", "Ing. eléctrica y electrónica ", "Ing. Industrial", "Metal mecánica", "Sistemas y computación", "Ciencias de la tierra", "Centro de computo", "Servicios escolares", "Centro de información", "Actividades extraescolares")
        val specialty = if(isStudent) students else teachers
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(this,android.R.layout.simple_selectable_list_item, specialty)

        binding.listSpecialty.adapter = arrayAdapter
        binding.txtSelectSpecialty.text = if(isStudent) "Selecciona tu carrera" else "Selecciona tu departamento"

        binding.listSpecialty.isSelected = false
        binding.listSpecialty.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                binding.btnSaveSpecialty.setOnClickListener {

                    showAlert(specialty[position], "Este dato no podrá ser cambiado, ¿estás seguro que es correcto?", isStudent)

                }//se dio click al boton de guardar

            }//onItemSelected

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }//onNothingSelected

        }//onItemSelectedListener

    }//setupRadioGroup

    private fun handleUpload(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        val uid: String = Methods.getEmail(this).toString()
        val ref: StorageReference = FirebaseStorage.getInstance().reference
            .child(storageChild)
            .child("$uid.jpeg")

        ref.putBytes(baos.toByteArray())
            .addOnSuccessListener {
                getDownloadUrl(ref)
            }
            .addOnFailureListener() {
                Log.e("Errorimg", "onFailure", it.cause)
            }
    }

    private fun showAlert(title:String, message:String, isStudent: Boolean){

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("Sí"
        ) { _, _ ->

            setSpecialty(title)

        }
        builder.setNegativeButton("No", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()

    }//show alert

    private fun setSpecialty(specialty: String) {

        db.collection("Users").document(currentUser!!).update("specialty", specialty).addOnSuccessListener {

            Toast.makeText(this, "Perfil completado", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, HomeActivity::class.java).apply {

                putExtra("email", currentUser)

            }//intent

            startActivity(intent)

        }//se actualizo la especialidad correctamente

    }//set specialty

    private fun getDownloadUrl(ref: StorageReference) {
        ref.downloadUrl.addOnSuccessListener {
            setUserProfileUrl(it)
        }
    }

    private fun setUserProfileUrl(uri: Uri) {
        val user: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        val request: UserProfileChangeRequest = UserProfileChangeRequest
            .Builder()
            .setPhotoUri(uri)
            .build()

        user.updateProfile(request)
            .addOnSuccessListener {
                db.collection("Users").document(Methods.getEmail(this).toString()).update(databaseChild,uri.toString())
                loadImg()
                Toast.makeText(this, "Actualización exitosa", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Actualización fallida", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadImg() {

        db.collection("Users").document(Methods.getEmail(this).toString()).addSnapshotListener{
                result, error ->
            val urlImg = result!!.get("imgProfile").toString()

            try {
                if(urlImg.isNotEmpty())
                    Picasso.get().load(urlImg).into(binding.civEditPhoto)

            } catch (e: Exception) {
                Picasso.get().load(R.drawable.ic_user_profile).into(binding.civEditPhoto)
            }
        }
    }

}