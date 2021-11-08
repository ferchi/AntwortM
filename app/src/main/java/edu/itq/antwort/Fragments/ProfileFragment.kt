package edu.itq.antwort.Fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import edu.itq.antwort.R
import edu.itq.antwort.Adapters.ViewPagerAdapter
import edu.itq.antwort.databinding.FragmentProfileBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import com.squareup.picasso.Picasso
import edu.itq.antwort.Activities.EditProfileActivity
import edu.itq.antwort.Classes.Users
import edu.itq.antwort.Methods

class ProfileFragment() : Fragment() {

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

                }

                "Eliminar cuenta" -> {

                }
            }
        }//onMenuItemClickListener

    private fun requestRol(user:String){

    }


}//class