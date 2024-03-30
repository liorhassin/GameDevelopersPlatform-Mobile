package com.example.gamedevelopersplatform

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_game_page, container, false)

        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
        selectedImageView = view.findViewById(R.id.addGamePageGameImage)

        //TODO - Add support to upload an image from device.
        //TODO - Complete Basic Loop.
        //Expected Loop for create button(saving a new game):
        //Upload image to Storage.
        //Retrieve saved image Url after successful save.
        //Save game data object in 'games' collection.
        //Retrieve game object Id from firebase collection and add it to user games.

        val releaseDateTextView: TextView = view.findViewById(R.id.addGamePageReleaseDateText)
        val priceTextInput: TextInputEditText = view.findViewById(R.id.addGamePagePriceInput)
        val gameNameTextInput: TextInputEditText = view.findViewById(R.id.addGamePageGameNameInput)

        //TODO - Add lambda expression for white call fix.
        //For : Game Name, Game Price Input, Release Date, Choose Image Button.

        galleryLauncher = generateGalleryLauncher { data -> handleSelectedImage(data) }
        view.findViewById<Button>(R.id.addGamePageChooseImageButton).setOnClickListener {
            GameDevelopersAppUtil.openGallery(galleryLauncher)
        }

        view.findViewById<Button>(R.id.addGamePageShowDatePickerButton).setOnClickListener {
            GameDevelopersAppUtil.showDatePicker(requireContext(), Calendar.getInstance()){formattedDate ->
                releaseDateTextView.text = formattedDate
            }
        }

        //TODO - Loop should be here, use helper functions when refactoring the code.
        view.findViewById<Button>(R.id.addGamePageCreateButton).setOnClickListener {
            val price = priceTextInput.text.toString()
            val name = gameNameTextInput.text.toString()


            val (validPrice, validName) = gameValidation(price,name)
            if(validPrice && validName){

            }
            else{
                if(!validPrice){
                    priceTextInput.setTextColor(Color.RED)
                }
                if(!validName){
                    gameNameTextInput.setTextColor(Color.RED)
                }
            }


        }

        return view
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

    private fun gameValidation(price:String, name:String): Pair<Boolean,Boolean>{
        //Price must start with '$' sign and be between 0-300(allowing 2 numbers after decimal point.
        val priceRegex = Regex("^\\\$?(?:[1-2]?\\d{1,2}|300)(?:\\.\\d{1,2})?\$")
        //Name must be of length 2 and support Alphabet/Numbers/One white space.
        val nameRegex = Regex("^(?=.*[A-Za-z].*[A-Za-z])[A-Za-z0-9_ ]{2,}\$")
        return Pair(priceRegex.matches(price), nameRegex.matches(name))
    }
}