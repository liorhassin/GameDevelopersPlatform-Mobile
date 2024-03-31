package com.example.gamedevelopersplatform

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
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
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Calendar

class AddGamePageFragment : Fragment() {
    //TODO - If parameters are not used in another function other than onCreateView, Change location.
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImageUri: Uri
    private lateinit var selectedImageView: ImageView

    private lateinit var releaseDateTextView: TextView
    private lateinit var priceTextInput: TextInputEditText
    private lateinit var gameNameTextInput: TextInputEditText
    private lateinit var chooseImageButton:Button
    private lateinit var showDatePickerButton: Button
    private lateinit var addGameButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_game_page, container, false)

        initializeParameters(view)
        addTextWatchers()
        setButtonsOnClickEvent()

        return view
    }

    private fun initializeParameters(view: View){
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
            if(data != null) GameDevelopersAppUtil.setTextAndHintTextColor(chooseImageButton, Color.WHITE)
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
            GameDevelopersAppUtil.showDatePicker(requireContext(), Calendar.getInstance()){formattedDate ->
                releaseDateTextView.text = formattedDate
            }
        }

        //TODO - Loop should be here, use helper functions when refactoring the code.
        addGameButton.setOnClickListener {
            val price = priceTextInput.text.toString()
            val name = gameNameTextInput.text.toString()
            val uploadedGamePicture = selectedImageView.drawable

            val (validPrice, validName, validPicture) = gameValidation(price,name, uploadedGamePicture)
            if(validPrice && validName && validPicture){
                //TODO - Complete saving loop to DB.
            }
            else{
                if(!validPrice){
                    priceTextInput.setTextColor(Color.RED)
                    priceTextInput.setHintTextColor(Color.RED)
                }
                if(!validName){
                    gameNameTextInput.setTextColor(Color.RED)
                    gameNameTextInput.setHintTextColor(Color.RED)
                }
                if(!validPicture){
                    chooseImageButton.setTextColor(Color.RED)
                }
            }
        }
    }

    private fun generateGalleryLauncher(callback: (Intent?)->Unit): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                callback(data)
            }
        }
    }

    private fun handleSelectedImage(data: Intent?) {
        data?.data?.let { uri ->
            selectedImageUri = uri
            selectedImageView.setImageURI(uri)
        }
    }

    private fun gameValidation(price:String, name:String, picture: Drawable?): Triple<Boolean,Boolean,Boolean>{
        //Price must start with '$' sign and be between 0-300(allowing 2 numbers after decimal point.
        val priceRegex = Regex("^\\\$?(?:[1-2]?\\d{1,2}|300)(?:\\.\\d{1,2})?\$")
        //Name must be of length 2 and support Alphabet/Numbers/One white space.
        val nameRegex = Regex("^(?=.*[A-Za-z].*[A-Za-z])[A-Za-z0-9_ ]{2,}\$")
        return Triple(priceRegex.matches(price), nameRegex.matches(name), picture!=null)
    }
}