package com.example.gamedevelopersplatform.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gamedevelopersplatform.R
import com.example.gamedevelopersplatform.databinding.ActivityInitializeNavbarBinding
import com.example.gamedevelopersplatform.fragments.AddGamePageFragment
import com.example.gamedevelopersplatform.fragments.HomePageFragment
import com.example.gamedevelopersplatform.fragments.MyGamesPageFragment
import com.example.gamedevelopersplatform.fragments.ProfilePageFragment
import com.example.gamedevelopersplatform.util.GameDevelopersGeneralUtil
import com.google.firebase.auth.FirebaseAuth

class InitializeNavbarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInitializeNavbarBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var selectedItem: Int = R.id.home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInitializeNavbarBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()

        setContentView(binding.root)
        replaceFragment(HomePageFragment())

        binding.bottomNavigationView.setOnItemSelectedListener {item->
            selectedItem = item.itemId
            when(item.itemId){
                R.id.home -> replaceFragment(HomePageFragment())
                R.id.profile -> replaceFragment(ProfilePageFragment.newInstance(firebaseAuth.currentUser?.uid.toString()))
                R.id.addGame -> replaceFragment(AddGamePageFragment())
                R.id.myGames -> replaceFragment(MyGamesPageFragment.newInstance(firebaseAuth.currentUser?.uid.toString()))
                R.id.logout -> {
                    if(firebaseAuth.currentUser != null){
                        val builder = AlertDialog.Builder(this)
                        builder.setPositiveButton("Yes"){_,_->
                            firebaseAuth.signOut()
                            Toast.makeText(this, "Logout...", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, SignInPageActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        builder.setNegativeButton("No"){_,_-> }
                        builder.setTitle("Logout?")
                        builder.create().show()
                    }
                }
                else ->{
                }
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}