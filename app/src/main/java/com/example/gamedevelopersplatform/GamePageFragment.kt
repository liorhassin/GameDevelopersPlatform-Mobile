package com.example.gamedevelopersplatform

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

//TODO - IN GAME PAGE
//initialize parameters
//load game data to view elements
//enable switching between via code
//only show button to switch between modes to owner of the game.
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
        addTextWatchers()
        setButtonsOnClickEvent()
        updateGamePageView()

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
            previewLayout.visibility = View.GONE
            editLayout.visibility = View.VISIBLE
        }
        previewDeleteButton.setOnClickListener {

        }

        editPickADateButton.setOnClickListener {

        }
        editChooseImageButton.setOnClickListener {

        }
        editSaveButton.setOnClickListener {

        }
        editCancelButton.setOnClickListener {
            editLayout.visibility = View.GONE
            previewLayout.visibility = View.VISIBLE
        }
    }

    private fun updateGamePageView(){
        previewNameView.text = name
        previewPriceView.text = price
        previewReleaseDateView.text = releaseDate
        GameDevelopersAppUtil.loadImageFromDB(storageRef, image, previewImageView)
        //TODO - FETCH -> Developer name using ID
        //gameDeveloperNamePreviewView.text =

        editNameInput.setText(name)
        editPriceInput.setText(price)
        editReleaseDateView.text = releaseDate
        GameDevelopersAppUtil.loadImageFromDB(storageRef, image, editImageView)
    }
}