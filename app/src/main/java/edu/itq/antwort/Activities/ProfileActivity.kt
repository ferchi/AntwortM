package edu.itq.antwort.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import edu.itq.antwort.Adapters.ViewPagerAdapter
import edu.itq.antwort.Fragments.AnswersFragment
import edu.itq.antwort.Fragments.ConsultFragment
import edu.itq.antwort.Fragments.NewsFragment
import edu.itq.antwort.Methods
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ActivityAnswerScreenBinding
import edu.itq.antwort.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileBinding
    lateinit var email: String
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        binding.includeToolbar.ivBack.visibility = View.VISIBLE
        binding.includeToolbar.imgProfileTB.visibility = View.INVISIBLE

        binding.includeToolbar.ivBack.setOnClickListener {
            onBackPressed()
        }

        val bundle = intent.extras
        email = bundle?.getString("email").toString()

        setUpTabs()
        db = FirebaseFirestore.getInstance()

        binding.btnNewQuestion.setOnClickListener {

            val currentUser = FirebaseAuth.getInstance().currentUser?.email

            val questionIntent = Intent(this, QuestionScreenActivity::class.java).apply {

                putExtra("email", currentUser)

            }//home intent

            startActivity(questionIntent)

        }//new Question

        getRol()
        loadImg()
        showAnalytics()

    }//onCreate

    private fun showAnalytics(){

        binding.btnStatistics.setOnClickListener {

            val intent = Intent(this, AnalyticsActivity::class.java).apply {

                putExtra("user", email)

            }//le pasamos el correo

            startActivity(intent)

        }//se presiono el boton de estadisticas

    }//showAnalytics

    override fun onStart() {
        super.onStart()
        updateInfo()
    }

    private fun setUpTabs() {

        val bundle = Bundle()
        bundle.putString("email", email)
        val frag1 = ConsultFragment()
        val frag2 = AnswersFragment()
        frag1.arguments = bundle
        frag2.arguments = bundle

        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(frag1, "Consultas")
        adapter.addFragment(frag2, "Respuestas")
        binding.viewPager.adapter = adapter

        binding.tabs.setupWithViewPager(binding.viewPager)
        binding.tabs.getTabAt(0)!!.setIcon(R.drawable.ic_hearing_24)
        binding.tabs.getTabAt(1)!!.setIcon(R.drawable.ic_question_24)
    }

    private fun getRol(){

        val queryRol = db.collection("Users").document(email)
        queryRol.get().addOnSuccessListener {
            val rol = it.get("rol") as String
            if (rol == "Facilitador") {
                binding.ivProfileVerification.visibility = View.VISIBLE
            }
        }
    }

    private fun updateInfo(){
        val query = db.collection("Users").whereEqualTo("email", email)

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
                            Firebase.auth.signOut()
                            Toast.makeText(this, "Cuenta eliminada, nos vemos pronto.", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this,Login::class.java)
                            startActivity(intent)
                            this.finish()
                        }}
            }
        }

    }

    private fun loadImg() {

        db.collection("Users").document(email).addSnapshotListener{
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

}//class