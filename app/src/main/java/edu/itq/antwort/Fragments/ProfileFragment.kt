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
import android.widget.Toast
import edu.itq.antwort.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    //val appContext : Context = requireContext().applicationContext

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        return inflater.inflate(R.layout.fragment_profile, container, false)

    }//onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)
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

}//class