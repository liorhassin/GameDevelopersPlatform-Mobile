package com.example.gamedevelopersplatform

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gamedevelopersplatform.databinding.ActivitySignUpPageBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpPageActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivitySignUpPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //TODO - Remove or edit how text watcher is being generated from helper function.
        binding.signUpNicknameInput.addTextChangedListener(generateTextWatcher(binding.signUpNicknameInput))
        binding.signUpPasswordInput.addTextChangedListener(generateTextWatcher(binding.signUpPasswordInput))
        binding.signUpEmailInput.addTextChangedListener(generateTextWatcher(binding.signUpEmailInput))

        binding.signUpButton.setOnClickListener {
            val nickname = binding.signUpNicknameInput.text.toString()
            val password = binding.signUpPasswordInput.text.toString()
            val email = binding.signUpEmailInput.text.toString()
            val (validNickname, validPassword, validEmail) = validation(nickname, password, email)
            if(validNickname && validPassword && validEmail){
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if(it.isSuccessful) {
                        saveUserToFirestore(nickname, password, email)
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, InitializeNavbarActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else Toast.makeText(this, it.exception?.message.toString(),Toast.LENGTH_SHORT).show()
                }
            }
            else{
                if(!validNickname){
                    binding.signUpNicknameInput.setTextColor(Color.RED)
                }
                if(!validPassword){
                    binding.signUpPasswordInput.setTextColor(Color.RED)
                }
                if(!validEmail){
                    binding.signUpEmailInput.setTextColor(Color.RED)
                }
            }
        }

    }

    private fun validation(nickname:String, password:String, email:String): Triple<Boolean, Boolean, Boolean>{
        //Nickname must be of at least length 2 and can only contain english alphabet/numbers/one white space between words.
        val nicknameRegex = Regex("^(?=.*[A-Za-z].*[A-Za-z])[A-Za-z0-9_ ]{2,}\$")

        //Password must be of at least length 6 and contain one Capital Letter/One Normal Letter/One Number/One Special Character.
        val passwordRegex = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!])(?=\\S+\$).{6,}\$")

        //Email must follow commonly accepted standards such as having a @ and contains supported characters before and after.
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$")

        return Triple(nicknameRegex.matches(nickname), passwordRegex.matches(password), emailRegex.matches(email))
    }

    private fun saveUserToFirestore(nickname:String, password:String, email:String){
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

    /***
     * Helper function to generate TextWatcher for textInput afterTextChanged effect.(Back to White)
     */
    private fun generateTextWatcher(textInput: TextInputEditText): TextWatcher {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Not required for this implementation
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Not required for this implementation
            }
            override fun afterTextChanged(p0: Editable?) {
                textInput.setTextColor(Color.WHITE)
            }
        }
        return textWatcher
    }

    //TODO - Add room (local cache)
}