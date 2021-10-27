package edu.itq.antwort.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.itq.antwort.R
import android.widget.Toast
import edu.itq.antwort.Adapters.ViewPagerAdapter
import edu.itq.antwort.databinding.FragmentProfileBinding

import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import edu.itq.antwort.Activities.EditProfileActivity
import edu.itq.antwort.Classes.Users
import edu.itq.antwort.Methods


class ProfileFragment() : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var current : String

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

        binding.btnProfileEdit.setOnClickListener {
            val editIntent = Intent(context, EditProfileActivity::class.java).apply {

                putExtra("current", current)

            }//homeIntent

            startActivity(editIntent)
        }
/*
        binding.btnLogOut.setOnClickListener {

            //val prefs : SharedPreferences =  requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
            //prefs.clear()
            showAlert("Boton cerrar sesión presionado")
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent (context, Login::class.java))

        }//setOnClickListener
*/
    }//onViewCreated

    override fun onStart() {
        super.onStart()
        updateInfo()
    }

    private fun showAlert( message:String){

        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.show()

    }//función show alert


    private fun setUpTabs() {

        //Utilizar el childFragmentManager para evitar errores dentro del tab layout
        val adapter = ViewPagerAdapter(childFragmentManager)

        adapter.addFragment(NewsFragment(), "Recientes")
        adapter.addFragment(ConsultFragment(), "Consultas")
        adapter.addFragment(AnswersFragment(), "Respuestas")

        binding.viewPager.adapter = adapter
        binding.tabs.setupWithViewPager(binding.viewPager)

        binding.tabs.getTabAt(0)!!.setIcon(R.drawable.ic_new_24)
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

        query.addSnapshotListener{   value, error ->
            val userInfo = value!!.documents[0]
            binding.tvProfileUsername.text = userInfo.get("name").toString()
            binding.tvProfileCountAnswer.text = userInfo.get("answers").toString()
            binding.tvProfileCountQuestion.text = userInfo.get("questions").toString()
        }
    }

    private fun loadImg() {

        db.collection("Users").document(Methods.getEmail(requireActivity()).toString()).addSnapshotListener{
            result, error ->
            val urlImg = result!!.get("imgProfile").toString()

            try {
                Picasso.get().load(urlImg).into(binding.civProfileImageProfile)

            } catch (e: Exception) {
                Picasso.get().load(R.drawable.ic_user_profile).into(binding.civProfileImageProfile)
            }
        }
    }

}//class