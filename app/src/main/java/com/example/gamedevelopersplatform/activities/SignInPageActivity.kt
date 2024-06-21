package com.example.gamedevelopersplatform.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gamedevelopersplatform.databinding.ActivitySignInPageBinding
import com.example.gamedevelopersplatform.util.GameDevelopersGeneralUtil
import com.google.firebase.auth.FirebaseAuth

class SignInPageActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivitySignInPageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.signInRegisterButton.setOnClickListener {
            val intent = Intent(this, SignUpPageActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.signInbutton.setOnClickListener {
            val email = binding.signInEmailInput.text.toString()
            val password = binding.signInPasswordInput.text.toString()
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnFailureListener {
                GameDevelopersGeneralUtil.popToast(this@SignInPageActivity, "Failed to authenticate, wrong Password or Email!", Toast.LENGTH_SHORT)
            }.addOnCompleteListener {
                if(it.isSuccessful){
                    val intent = Intent(this, InitializeNavbarActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}