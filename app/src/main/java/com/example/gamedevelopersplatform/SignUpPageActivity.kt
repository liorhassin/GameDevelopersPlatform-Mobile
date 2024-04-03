package com.example.gamedevelopersplatform

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.gamedevelopersplatform.databinding.ActivitySignUpPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Calendar
//TODO - Change xml parameters to be signUpPage.. Like the rest of the xml convention.
class SignUpPageActivity : AppCompatActivity() {
    data class QuadrupleBooleans(val first: Boolean, val second: Boolean, val third: Boolean, val fourth: Boolean)
    private val USERS_PROFILE_IMAGES_PATH = "UsersProfileImages/"

    private lateinit var binding: ActivitySignUpPageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: StorageReference

    private var selectedImageUri: Uri? = null
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeParameters()
        addTextWatchers()
        setButtonsOnClickEvent()
    }

    private fun initializeParameters(){
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        galleryLauncher = generateGalleryLauncher {
                data -> handleSelectedImage(data)
            if(data != null) GameDevelopersAppUtil.setTextAndHintTextColor(binding.signUpPageChooseImageButton, Color.WHITE)
        }
    }

    private fun addTextWatchers() {
        GameDevelopersAppUtil.handleTextChange(binding.signUpNicknameInput){
            GameDevelopersAppUtil.setTextAndHintTextColor(binding.signUpNicknameInput, Color.WHITE)
        }
        GameDevelopersAppUtil.handleTextChange(binding.signUpPasswordInput){
            GameDevelopersAppUtil.setTextAndHintTextColor(binding.signUpPasswordInput, Color.WHITE)
        }
        GameDevelopersAppUtil.handleTextChange(binding.signUpEmailInput){
            GameDevelopersAppUtil.setTextAndHintTextColor(binding.signUpEmailInput, Color.WHITE)
        }
    }

    private fun setButtonsOnClickEvent() {
        binding.signUpPickADateButton.setOnClickListener {
            GameDevelopersAppUtil.showDatePicker(this, Calendar.getInstance()){formattedDate ->
                binding.signUpBirthdateText.text = formattedDate
            }
        }

        binding.signUpPageChooseImageButton.setOnClickListener {
            GameDevelopersAppUtil.openGallery(galleryLauncher)
        }

        binding.signUpButton.setOnClickListener {
            val nickname = binding.signUpNicknameInput.text.toString()
            val password = binding.signUpPasswordInput.text.toString()
            val email = binding.signUpEmailInput.text.toString()
            val birthDate = binding.signUpBirthdateText.text.toString()

            val validationQuadruple = userValidation(nickname, password, email, selectedImageUri)
            val validNickname = validationQuadruple.first
            val validPassword = validationQuadruple.second
            val validEmail = validationQuadruple.third
            val validPicture = validationQuadruple.fourth

            if(validNickname && validPassword && validEmail && validPicture){
                createUserAndSaveData(nickname, password, email, birthDate)
            }
            else{
                markMissingInputsColor(validNickname, validPassword, validEmail, validPicture)
            }
        }
    }

    private fun markMissingInputsColor(validNickname: Boolean, validPassword: Boolean, validEmail: Boolean, validPicture: Boolean){
        if(!validNickname)
            GameDevelopersAppUtil.setTextAndHintTextColor(binding.signUpNicknameInput,Color.RED)
        if(!validPassword)
            GameDevelopersAppUtil.setTextAndHintTextColor(binding.signUpPasswordInput, Color.RED)
        if(!validEmail)
            GameDevelopersAppUtil.setTextAndHintTextColor(binding.signUpEmailInput, Color.RED)
        if(!validPicture)
            GameDevelopersAppUtil.setTextAndHintTextColor(binding.signUpPageChooseImageButton, Color.RED)
    }

    private fun generateGalleryLauncher(callback: (Intent?)->Unit): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                callback(data)
            }
        }
    }

    private fun saveImageAndUserData(nickname: String, email: String, birthDate: String){
        GameDevelopersAppUtil.uploadImageAndGetUrl(storageRef, USERS_PROFILE_IMAGES_PATH,
            selectedImageUri!!,{ imageUrl ->
                val userData = hashMapOf(
                    "nickname" to nickname,
                    "email" to email,
                    "birthDate" to birthDate,
                    "profileImage" to imageUrl,
                    "userGames" to emptyList<String>()
                )
                val currentUser = firebaseAuth.currentUser
                if(currentUser!=null){
                    val userId = currentUser.uid
                    saveUserData(userData, userId)
                }
            },{

            })
    }

    private fun handleSelectedImage(data: Intent?) {
        data?.data?.let { uri ->
            selectedImageUri = uri
            binding.signUpPageCircleImagePreview.setImageURI(uri)
        }
    }

    private fun userValidation(nickname:String, password:String, email:String, pictureUri:Uri?)
    : QuadrupleBooleans{

        //Nickname must be of at least length 2 and can only contain english alphabet/numbers/one white space between words.
        val nicknameRegex = Regex("^(?=.*[A-Za-z].*[A-Za-z])[A-Za-z0-9_ ]{2,}\$")
        //Password must be of at least length 6 and contain one Capital Letter/One Normal Letter/One Number/One Special Character.
        val passwordRegex = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!])(?=\\S+\$).{6,}\$")
        //Email must follow commonly accepted standards such as having a @ and contains supported characters before and after.
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$")

        return QuadrupleBooleans(
            nicknameRegex.matches(nickname),
            passwordRegex.matches(password),
            emailRegex.matches(email),
            pictureUri != null
        )
    }

    private fun createUserAndSaveData(nickname:String, password:String, email:String, birthDate:String){
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if(it.isSuccessful) {
                saveImageAndUserData(nickname, email, birthDate)
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                moveToHomePageFragment()
            }
            else Toast.makeText(this, it.exception?.message.toString(),Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveToHomePageFragment() {
        val intent = Intent(this, InitializeNavbarActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveUserData(userData: HashMap<String, Any>, userId: String){
        firestore.collection("users").document(userId).set(userData).addOnCompleteListener {
            if(it.isSuccessful){
                Log.d("success", "User was saved successfully to Firestore")
            }
            else{
                Log.d("fail", it.exception?.message.toString())
            }
        }
    }

    //TODO - Add room (local cache)
}