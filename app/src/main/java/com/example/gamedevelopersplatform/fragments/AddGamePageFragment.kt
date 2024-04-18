package com.example.gamedevelopersplatform.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.gamedevelopersplatform.util.GameDevelopersAppUtil
import com.example.gamedevelopersplatform.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Calendar

class AddGamePageFragment : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: StorageReference

    private var selectedImageUri: Uri? = null
    private lateinit var selectedImageView: ImageView
    private lateinit var releaseDateTextView: TextView
    private lateinit var priceTextInput: TextInputEditText
    private lateinit var gameNameTextInput: TextInputEditText
    private lateinit var chooseImageButton:Button
    private lateinit var showDatePickerButton: Button
    private lateinit var addGameButton: Button

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_game_page, container, false)

        initializeParameters(view)
        addTextWatchers()
        setButtonsOnClickEvent()

        return view
    }

    private fun initializeParameters(view: View){
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        selectedImageView = view.findViewById(R.id.addGamePageGameImage)
        releaseDateTextView = view.findViewById(R.id.addGamePageReleaseDateText)
        priceTextInput = view.findViewById(R.id.addGamePagePriceInput)
        gameNameTextInput = view.findViewById(R.id.addGamePageGameNameInput)
        chooseImageButton = view.findViewById(R.id.addGamePageChooseImageButton)
        showDatePickerButton = view.findViewById(R.id.addGamePageShowDatePickerButton)
        addGameButton = view.findViewById(R.id.addGamePageCreateButton)

        galleryLauncher = generateGalleryLauncher {
                data -> handleSelectedImage(data)
            if(data != null) GameDevelopersAppUtil.setTextAndHintTextColor(
                chooseImageButton,
                Color.WHITE
            )
        }
    }

    private fun addTextWatchers(){
        GameDevelopersAppUtil.handleTextChange(releaseDateTextView) {
            GameDevelopersAppUtil.setTextAndHintTextColor(releaseDateTextView, Color.WHITE)
        }
        GameDevelopersAppUtil.handleTextChange(priceTextInput) {
            GameDevelopersAppUtil.setTextAndHintTextColor(priceTextInput, Color.WHITE)
        }
        GameDevelopersAppUtil.handleTextChange(gameNameTextInput) {
            GameDevelopersAppUtil.setTextAndHintTextColor(gameNameTextInput, Color.WHITE)
        }
    }

    private fun setButtonsOnClickEvent(){
        chooseImageButton.setOnClickListener {
            GameDevelopersAppUtil.openGallery(galleryLauncher)
        }

        showDatePickerButton.setOnClickListener {
            GameDevelopersAppUtil.showDatePicker(
                requireContext(),
                Calendar.getInstance()
            ) { formattedDate ->
                releaseDateTextView.text = formattedDate
            }
        }
        addGameButton.setOnClickListener {
            val price = priceTextInput.text.toString()
            val name = gameNameTextInput.text.toString()
            val releaseDate = releaseDateTextView.text.toString()
            val uid = firebaseAuth.currentUser?.uid.toString()

            val (validPrice, validName, validPicture) = gameValidation(price,name, selectedImageUri)
            if(validPrice && validName && validPicture){
                saveImageAndGameData(name, price, releaseDate, uid)
            }
            else{
                markMissingInputsColor(validPrice, validName, validPicture)
            }
        }
    }

    private fun saveImageAndGameData(name:String, price:String, releaseDate:String, uid:String){
        GameDevelopersAppUtil.uploadImageAndGetName(storageRef,
            GameDevelopersAppUtil.GAMES_IMAGES_PATH,
            selectedImageUri!!,
            { imageUrl ->
                val gameData = hashMapOf(
                    "image" to imageUrl,
                    "name" to name,
                    "price" to price,
                    "releaseDate" to releaseDate,
                    "developerId" to uid
                )
                saveGameDataAndGetGameId(gameData,
                    { gameId ->
                        saveGameId(gameId, uid, {
                            GameDevelopersAppUtil.changeFragmentFromFragment(
                                requireActivity(),
                                R.id.addGamePageLayout,
                                MyGamesPageFragment.newInstance(uid)
                            )
                        }, { exception ->
                            Log.e(
                                "SavingGameId",
                                "Failed to save gameId to userGames array: $exception"
                            )
                        })
                    }
                ) { exception ->
                    Log.e("UploadGame", "Failed to upload game data: $exception")
                }
            },
            { exception ->
                Log.e("UploadImage", "Failed to upload image: $exception")
            }
        )
    }

    private fun saveGameId(gameId: String, uid: String,
        onSuccess: () -> Unit, onFailure: (exception: Exception) -> Unit) {
        val firestoreUserDocument = firestore.collection("users").document(uid)

        firestoreUserDocument.update("userGames", FieldValue.arrayUnion(gameId))
            .addOnSuccessListener {
                Log.d("saveGameId", "Game ID added successfully")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.w("saveGameId", "Error adding game ID", exception)
                onFailure(exception)
            }
    }

    private fun markMissingInputsColor(validPrice: Boolean, validName: Boolean, validPicture: Boolean){
        if(!validPrice)
            GameDevelopersAppUtil.setTextAndHintTextColor(priceTextInput, Color.RED)
        if(!validName)
            GameDevelopersAppUtil.setTextAndHintTextColor(gameNameTextInput, Color.RED)
        if(!validPicture)
            GameDevelopersAppUtil.setTextAndHintTextColor(chooseImageButton, Color.RED)
    }

    //TODO - consider making this method generic for util object
    private fun generateGalleryLauncher(callback: (Intent?)->Unit): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                callback(data)
            }
        }
    }

    //TODO - consider making this method generic for util object.
    private fun handleSelectedImage(data: Intent?) {
        data?.data?.let { uri ->
            selectedImageUri = uri
            selectedImageView.setImageURI(uri)
        }
    }

    private fun saveGameDataAndGetGameId(gameData: HashMap<String, String>,
        onSuccess: (gameId: String) -> Unit, onFailure: (exception: Exception) -> Unit) {
        firestore.collection("games").add(gameData)
            .addOnSuccessListener { documentReference ->
                val gameId = documentReference.id
                onSuccess(gameId)
            }.addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    private fun gameValidation(price:String, name:String, pictureUri: Uri?): Triple<Boolean,Boolean,Boolean>{
        return Triple(
            GameDevelopersAppUtil.gamePriceValidation(price),
            GameDevelopersAppUtil.gameNameValidation(name),
            pictureUri!=null)
    }
}