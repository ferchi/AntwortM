package edu.itq.antwort.Activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
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
    private lateinit var current:String

    // Variables para obtener y cambiar de las imagenes de perfil
    val TAKE_IMG_CODE = 1046
    lateinit var vista: View
    lateinit var storageChild: String
    lateinit var databaseChild: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        Log.d("name", Methods.getEmail(this)!!)
        getName()

        binding.civEditPhoto.setOnClickListener {
            changeImg()
        }

        binding.btnEditUsername.setOnClickListener {
            db.collection("Users").document(Methods.getEmail(this)!!).update("name", binding.etEditUsername.text.toString())
            Methods.customToast(this,"Actualizado")
            onBackPressed()
        }
        loadImg()

    }

    private fun getName(){
        val queryRol = db.collection("Users").document(Methods.getEmail(this)!!)
        queryRol.get().addOnCompleteListener(
            OnCompleteListener<DocumentSnapshot>() {
                val name = it.result!!.toObject(Users::class.java)!!.name

                binding.etEditUsername.hint = name
            }
        )
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
                Picasso.get().load(urlImg).into(binding.civEditPhoto)

            } catch (e: Exception) {
                Picasso.get().load(R.drawable.ic_user_profile).into(binding.civEditPhoto)
            }
        }
    }

}