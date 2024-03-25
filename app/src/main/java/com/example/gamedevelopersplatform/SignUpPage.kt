package com.example.gamedevelopersplatform

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gamedevelopersplatform.databinding.ActivitySignUpPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpPage : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivitySignUpPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.signUpButton.setOnClickListener {
            val nickname = binding.nicknameInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val email = binding.emailInput.text.toString()
            if(validation(nickname, password, email)){
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if(it.isSuccessful) {
                        saveUserToFirestore(nickname, password, email)
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, InitializeNavbar::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else Toast.makeText(this, it.exception?.message.toString(),Toast.LENGTH_SHORT).show()
                }
            }
            else{
                //TODO - Show red error where validation failed.
            }
        }

    }

    fun validation(nickname:String, password:String, email:String): Boolean{
        return true
    }

    fun saveUserToFirestore(nickname:String, password:String, email:String){
        val user = hashMapOf(
            "Nickname" to nickname,
            "Password" to password,
            "Email" to email
        )
        val currentUser = firebaseAuth.currentUser
        if(currentUser!=null){
            val userId = currentUser.uid
            firestore.collection("Users").document(userId).set(user).addOnCompleteListener {
                if(it.isSuccessful){
                    Log.d("success", "User was saved successfully to Firestore")
                }
                else{
                    Log.d("fail", it.exception?.message.toString())
                }
            }
        }
    }

    //TODO - Add room (local cache)
}