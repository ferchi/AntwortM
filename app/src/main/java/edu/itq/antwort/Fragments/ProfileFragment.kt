package edu.itq.antwort.Fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import edu.itq.antwort.R
import edu.itq.antwort.Adapters.ViewPagerAdapter
import edu.itq.antwort.databinding.FragmentProfileBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import com.squareup.picasso.Picasso
import edu.itq.antwort.Activities.EditProfileActivity
import edu.itq.antwort.Activities.Login
import edu.itq.antwort.Classes.Users
import edu.itq.antwort.Methods
import edu.itq.antwort.Methods.sendEmail

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var current : String
    private lateinit var popUpMenu :  PowerMenu.Builder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        return inflater.inflate(R.layout.fragment_profile, container, false)

    }//onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)
        current = Methods.getEmail(requireActivity()).toString()

        setUpTabs()
        db = FirebaseFirestore.getInstance()

        getRol()
        loadImg()

        popUpMenu = PowerMenu.Builder(requireContext())

        binding.btnProfileEdit.setOnClickListener {
            val editIntent = Intent(context, EditProfileActivity::class.java).apply {

                putExtra("current", current)

            }//homeIntent

            startActivity(editIntent)
        }

        binding.includeToolbar.imgProfileTB.setOnClickListener {
            createPopUp()
            popUpMenu.build().showAsDropDown(it)
        }

    }//onViewCreated

    override fun onStart() {
        super.onStart()
        updateInfo()
    }

    private fun setUpTabs() {

        //Utilizar el childFragmentManager para evitar errores dentro del tab layout
        val adapter = ViewPagerAdapter(childFragmentManager)

        adapter.addFragment(NewsFragment(), "Topics")
        adapter.addFragment(ConsultFragment(), "Consultas")
        adapter.addFragment(AnswersFragment(), "Respuestas")

        binding.viewPager.adapter = adapter
        binding.tabs.setupWithViewPager(binding.viewPager)

        binding.tabs.getTabAt(0)!!.setIcon(R.drawable.ic_baseline_topic_24)
        binding.tabs.getTabAt(1)!!.setIcon(R.drawable.ic_hearing_24)
        binding.tabs.getTabAt(2)!!.setIcon(R.drawable.ic_question_24)

    }

    private fun getRol(){

        val queryRol = db.collection("Users").document(current)
        queryRol.get().addOnCompleteListener {
            val rol = it.result!!.toObject(Users::class.java)!!.rol
            if (rol == "facilitador") {
                binding.ivProfileVerification.visibility = View.VISIBLE
            }
        }
    }

    private fun updateInfo(){
        val query = db.collection("Users").whereEqualTo("email", current)

        query.addSnapshotListener { value, _ ->
            try {
                val userInfo = value!!.documents[0]
                binding.tvProfileUsername.text = userInfo.get("name").toString()
                binding.tvProfileCountAnswer.text = userInfo.get("answers").toString()
                binding.tvProfileCountQuestion.text = userInfo.get("questions").toString()
            } catch (e: Exception) {

                val user = Firebase.auth.currentUser!!
                user.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //Firebase.auth.signOut()
                            Toast.makeText(requireContext(), "Cuenta eliminada, nos vemos pronto.", Toast.LENGTH_SHORT).show()
                            requireActivity().finish()
                            val intent = Intent(requireContext(),Login::class.java)
                            startActivity(intent)

                        }}
            }
        }

    }

    private fun loadImg() {

        db.collection("Users").document(Methods.getEmail(requireActivity()).toString()).addSnapshotListener{
                result, _ ->

            val urlImg = result!!.get("imgProfile").toString()

            try {

                if(urlImg.isNotEmpty())
                    Picasso.get().load(urlImg).into(binding.civProfileImageProfile)

            } catch (e: Exception) {
                Picasso.get().load(R.drawable.ic_user_profile).into(binding.civProfileImageProfile)
            }
        }
    }

    private fun createPopUp(){

        val context = requireContext()
        popUpMenu = PowerMenu.Builder(requireContext())
        popUpMenu
            .addItem(PowerMenuItem("Quiero ser Facilitador", false))
            .addItem(PowerMenuItem("Cerrar sesión", false))
            .addItem(PowerMenuItem("Eliminar cuenta", false))

            .setAnimation(MenuAnimation.FADE) // Animation start point (TOP | LEFT).
            .setMenuRadius(10f) // sets the corner radius.
            .setMenuShadow(10f) // sets the shadow.
            .setTextColor(ContextCompat.getColor(context, R.color.black))
            .setTextGravity(Gravity.CENTER)
            //.setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            .setSelectedTextColor(Color.WHITE)
            .setMenuColor(Color.WHITE)
            .setSelectedMenuColor(ContextCompat.getColor(context, R.color.orange))
            .setOnMenuItemClickListener(onMenuItemClickListener)
            .setAutoDismiss(true)
            .build()
    }

    private val onMenuItemClickListener: OnMenuItemClickListener<PowerMenuItem?> =
        OnMenuItemClickListener<PowerMenuItem?> { _, item ->

            when(item.title)
            {
                "Quiero ser Facilitador" -> {
                    requestRol(current)
                }
                "Cerrar sesión" -> {
                    closeSession()
                }

                "Eliminar cuenta" -> {
                    showAlert()
                }
            }
        }//onMenuItemClickListener

    private fun requestRol(user:String){

        val body = "¡Hola administrador! \n " +
                "Un usuario solicita obtener el rol de facilitador, tu decides si se lo otorgas. ;) \n" +
                "Saludos.\n\n" +
                "Usuario que realizó la solicitud: $user"


        sendEmail("fsalinas628@gmail.com", "Solicitud de rol",body, requireActivity())
        sendEmail("ramirezguillermo19@gmail.com", "Solicitud de rol",body, requireActivity())
    }

    private fun showAlert(){

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Eliminar cuenta")
        builder.setMessage("¿Esta seguro que desea elimiar su cuenta?")
        builder.setPositiveButton("Sí"
        ) { _, _ ->

            deleteUser()
        }
        builder.setNegativeButton("No", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()

    }//show alert

    private fun closeSession(){

        db.collection("Users").document(current).update("token", "").addOnSuccessListener {

            Firebase.auth.signOut()
            val preferences = requireActivity().getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
            val prefs = requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)

            preferences.edit().clear().apply()
            prefs.edit().clear().apply()

            val intent = Intent(requireContext(),Login::class.java)
            startActivity(intent)
            requireActivity().finish()

        }//eliminamos el token

    }

    private fun deleteUser(){

        db.collection("Questions").whereEqualTo("author",current).get().addOnCompleteListener {
            it.result!!.documents.forEach { document ->
                db.collection("Answers").whereEqualTo("question", document.id).get()
                    .addOnCompleteListener {
                        it.result!!.documents.forEach { document ->
                            db.collection("Answers").document(document.id).delete()
                        }
                    }
                db.collection("Questions").document(document.id).delete()
            }
        }

            Log.d("current", current)

        db.collection("Answers").whereEqualTo("author",current).get().addOnCompleteListener {
            it.result!!.documents.forEach { document ->
                db.collection("Questions").document(document.get("question").toString()).update("anwers", FieldValue.increment(-1))
                db.collection("Answers").document(document.id).delete()
            }
        }

        db.collection("Notifications").whereEqualTo("user",current).get().addOnCompleteListener {
            it.result!!.documents.forEach { document ->
                db.collection("Notifications").document(document.id).delete()
            }

            db.collection("Users").document(current).delete().addOnCompleteListener {  }
        }
    }

}//class