package edu.itq.antwort.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import edu.itq.antwort.Activitys.Login
import edu.itq.antwort.R
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.ColorFilter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import edu.itq.antwort.Adapters.ViewPagerAdapter
import edu.itq.antwort.databinding.FragmentProfileBinding
import androidx.core.graphics.drawable.DrawableCompat

import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.itq.antwort.Adapters.QuestionAdapter
import edu.itq.antwort.Classes.Questions
import edu.itq.antwort.Classes.Users
import edu.itq.antwort.Utils


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
        current = Utils.getEmail(requireActivity()).toString()

        setUpTabs()
        db = FirebaseFirestore.getInstance()

        val current = Utils.getEmail(requireActivity())

        val query = db.collection("Users").whereEqualTo("email", current)

        query.addSnapshotListener{   value, error ->
            binding.tvProfileUsername.text = value!!.documents[0].get("name").toString()
        }

        getRol()

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
        queryRol.get().addOnCompleteListener(
            OnCompleteListener<DocumentSnapshot>() {
                val rol = it.result!!.toObject(Users::class.java)!!.rol
                if(rol == "facilitador"){
                    binding.ivProfileVerification.visibility = View.VISIBLE
                }
            }
        )
    }

}//class