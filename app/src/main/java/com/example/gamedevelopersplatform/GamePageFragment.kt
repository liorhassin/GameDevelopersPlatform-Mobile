package com.example.gamedevelopersplatform

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Calendar

//TODO - IN GAME PAGE
//allow edit (name, image, release date, price)
//allow deletion of the project.

class GamePageFragment : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: StorageReference

    private lateinit var connectedUserId: String
    private lateinit var image: String
    private lateinit var name: String
    private lateinit var price: String
    private lateinit var releaseDate: String
    private lateinit var developerId: String
    private var developerName: String = "Unknown"
    private var isOwner = false

    //Preview
    private lateinit var previewLayout: ConstraintLayout

    private lateinit var previewNameView: TextView
    private lateinit var previewPriceView: TextView
    private lateinit var previewReleaseDateView: TextView
    private lateinit var previewImageView: ShapeableImageView
    private lateinit var previewDeveloperNameView: TextView
    private lateinit var previewDeveloperProfileButton: Button
    private lateinit var previewEditButton: Button
    private lateinit var previewDeleteButton: Button


    //Edit
    private lateinit var editLayout: ConstraintLayout

    private lateinit var editNameInput: TextInputEditText
    private lateinit var editPriceInput: TextInputEditText
    private lateinit var editReleaseDateView: TextView
    private lateinit var editImageView: ShapeableImageView
    private lateinit var editPickADateButton: Button
    private lateinit var editChooseImageButton: Button
    private lateinit var editSaveButton: Button
    private lateinit var editCancelButton: Button

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImageUri: Uri


    companion object{
        fun newInstance(gameData: GameData) = GamePageFragment().apply {
            arguments = bundleOf(
                "IMAGE" to gameData.image,
                "NAME" to gameData.name,
                "PRICE" to gameData.price,
                "RELEASE_DATE" to gameData.releaseDate,
                "DEVELOPER_ID" to gameData.developerId
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_game_page, container, false)

        initializeParameters(view)
        toggleOwnerButtons()
        addTextWatchers()
        setButtonsOnClickEvent()
        updateGamePagePreviewView()

        return view
    }

    private fun initializeParameters(view: View) {
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        connectedUserId = firebaseAuth.currentUser?.uid.toString()
        image = arguments?.getString("IMAGE", "").toString()
        name = arguments?.getString("NAME", "").toString()
        price = arguments?.getString("PRICE", "").toString()
        releaseDate = arguments?.getString("RELEASE_DATE", "").toString()
        developerId = arguments?.getString("DEVELOPER_ID", "").toString()
        isOwner = (connectedUserId == developerId)

        //Preview:
        previewLayout = view.findViewById(R.id.gamePagePreviewLayout)

        previewNameView = view.findViewById(R.id.gamePagePreviewGameTitle)
        previewPriceView = view.findViewById(R.id.gamePagePreviewPriceText)
        previewReleaseDateView = view.findViewById(R.id.gamePagePreviewReleaseDateText)
        previewImageView = view.findViewById(R.id.gamePagePreviewGameImage)
        previewDeveloperNameView = view.findViewById(R.id.gamePagePreviewDeveloperText)
        previewDeveloperProfileButton = view.findViewById(R.id.gamePagePreviewDeveloperProfileButton)
        previewEditButton = view.findViewById(R.id.gamePagePreviewEditButton)
        previewDeleteButton = view.findViewById(R.id.gamePagePreviewDeleteButton)

        //Edit:
        editLayout = view.findViewById(R.id.gamePageEditLayout)

        editNameInput = view.findViewById(R.id.gamePageEditNameInput)
        editPriceInput = view.findViewById(R.id.gamePageEditPriceInput)
        editReleaseDateView = view.findViewById(R.id.gamePageEditReleaseDateText)
        editImageView = view.findViewById(R.id.gamePageEditGameImage)
        editPickADateButton = view.findViewById(R.id.gamePageEditChooseReleaseDateButton)
        editChooseImageButton = view.findViewById(R.id.gamePageEditChooseImageButton)
        editSaveButton = view.findViewById(R.id.gamePageEditSaveButton)
        editCancelButton = view.findViewById(R.id.gamePageEditCancelButton)

        galleryLauncher = generateGalleryLauncher {
                data -> handleSelectedImage(data)
        }
    }

    private fun addTextWatchers(){
        GameDevelopersAppUtil.handleTextChange(editNameInput){
            GameDevelopersAppUtil.setTextAndHintTextColor(editNameInput, Color.WHITE)
        }
        GameDevelopersAppUtil.handleTextChange(editPriceInput){
            GameDevelopersAppUtil.setTextAndHintTextColor(editPriceInput, Color.WHITE)
        }
    }

    private fun setButtonsOnClickEvent(){
        previewDeveloperProfileButton.setOnClickListener {

        }
        previewEditButton.setOnClickListener {
            updateGamePageEditView()
            previewLayout.visibility = View.GONE
            editLayout.visibility = View.VISIBLE
        }
        previewDeleteButton.setOnClickListener {

        }

        editPickADateButton.setOnClickListener {
            GameDevelopersAppUtil.showDatePicker(this.requireContext(), Calendar.getInstance()){formattedDate ->
                editReleaseDateView.text = formattedDate
            }
        }
        editChooseImageButton.setOnClickListener {
            GameDevelopersAppUtil.openGallery(galleryLauncher)
        }
        editSaveButton.setOnClickListener {
            //TODO - Call save method that is split into organized functions.
            //TODO - Only after re-factoring save profile functions.
        }
        editCancelButton.setOnClickListener {
            editLayout.visibility = View.GONE
            previewLayout.visibility = View.VISIBLE
        }
    }

    private fun updateGamePagePreviewView(){
        previewNameView.text = name
        previewPriceView.text = "$ " + price
        previewReleaseDateView.text = releaseDate
        GameDevelopersAppUtil.loadImageFromDB(storageRef, image,
            GameDevelopersAppUtil.GAMES_IMAGES_PATH, previewImageView)
        retrieveAndLoadDeveloperNickname()
    }

    private fun updateGamePageEditView(){
        editNameInput.setText(name)
        editPriceInput.setText(price)
        editReleaseDateView.text = releaseDate
    }

    //TODO - consider moving function to generic util object (Used in a few places).
    private fun generateGalleryLauncher(callback: (Intent?)->Unit): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                callback(data)
            }
        }
    }

    //TODO - consider moving function to generic util object
    private fun handleSelectedImage(data: Intent?) {
        data?.data?.let { uri ->
            selectedImageUri = uri
            editImageView.setImageURI(uri)
        }
    }
    private fun toggleOwnerButtons(){
        if(isOwner){
            previewEditButton.visibility = View.VISIBLE
            previewDeleteButton.visibility = View.VISIBLE
        }else{
            previewEditButton.visibility = View.GONE
            previewDeleteButton.visibility = View.GONE
        }
    }

    private fun retrieveAndLoadDeveloperNickname(){
        firestore.collection("users").document(developerId).get().addOnSuccessListener { it->
            if(it.exists()){
                developerName = it.get("nickname").toString()
            }
            previewDeveloperNameView.text = developerName
        }
    }
}