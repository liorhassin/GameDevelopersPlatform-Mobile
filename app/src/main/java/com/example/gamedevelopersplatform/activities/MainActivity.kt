package com.example.gamedevelopersplatform.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.gamedevelopersplatform.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val user = firebaseAuth.currentUser
            if( user != null){
                val intent = Intent(this, InitializeNavbarActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this, SignInPageActivity::class.java)
                startActivity(intent)
                finish()
            }
        },3000)
    }
}