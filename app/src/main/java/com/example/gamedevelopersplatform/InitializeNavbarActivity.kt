package com.example.gamedevelopersplatform

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gamedevelopersplatform.databinding.ActivityInitializeNavbarBinding
import com.google.firebase.auth.FirebaseAuth

class InitializeNavbarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInitializeNavbarBinding
    private  lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInitializeNavbarBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()

        setContentView(binding.root)
        replaceFragment(HomePageFragment())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> replaceFragment(HomePageFragment())
                R.id.profile -> replaceFragment(ProfilePageFragment())
                R.id.addProject -> replaceFragment(AddGamePageFragment())
                R.id.myProjects -> replaceFragment(MyGamesPageFragment())
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