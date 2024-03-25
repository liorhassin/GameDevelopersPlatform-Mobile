package com.example.gamedevelopersplatform

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.gamedevelopersplatform.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    //TODO - Fix crash (firebaseAuth uninitialized error)!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val user = firebaseAuth.currentUser
            if( user != null){
                val intent = Intent(this, HomePage::class.java)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this, SignInPage::class.java)
                startActivity(intent)
                finish()
            }
        },3000)

    }
}