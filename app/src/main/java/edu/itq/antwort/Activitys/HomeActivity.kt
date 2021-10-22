package edu.itq.antwort.Activitys

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.itq.antwort.Fragments.HomeFragment
import edu.itq.antwort.Fragments.NotificationFragment
import edu.itq.antwort.Fragments.ProfileFragment
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var homeFragment: HomeFragment
    private lateinit var notificationFragment: NotificationFragment
    private lateinit var profileFragment: ProfileFragment

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bundle=intent.extras
        val email=bundle?.getString("email")

        //ToolbarSearchAdapter().show(this,"",true)
        val layoutManager = LinearLayoutManager(this)

        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true

        setup()
        newQuestion(email)

        // Guardado de datos

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.apply()

    }//fun onCreate

    private fun setup(){

        homeFragment = HomeFragment()
        supportFragmentManager.
        beginTransaction()
            .replace(R.id.frameLayout, homeFragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()

        binding.BottomNavBar.setOnItemSelectedListener { item->

            when(item.itemId){

                R.id.homeButton ->{
                    homeFragment = HomeFragment()
                    supportFragmentManager.
                    beginTransaction()
                        .replace(R.id.frameLayout, homeFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit()
                }//homeButton

                R.id.notificationButton ->{
                    notificationFragment = NotificationFragment()
                    supportFragmentManager.
                    beginTransaction()
                        .replace(R.id.frameLayout, notificationFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit()
                }//searchButton

                R.id.profileButton ->{
                    profileFragment = ProfileFragment()
                    supportFragmentManager.
                    beginTransaction()
                        .replace(R.id.frameLayout, profileFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit()
                }//searchButton

            }//when

            true

        }//setOnNavigationItemSelectedListener

    }//fun setup

    private fun newQuestion(email: String?){

        val btnNewQuestion: FloatingActionButton = findViewById(R.id.btnNewQuestion)

        btnNewQuestion.setOnClickListener {

            val questionIntent = Intent(this, QuestionScreenActivity::class.java).apply {

                putExtra("email", email)

            }//home intent

            startActivity(questionIntent)

        }//setOnCLick

    }//fun newQuestion

}//class