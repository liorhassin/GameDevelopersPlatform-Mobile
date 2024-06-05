package com.example.gamedevelopersplatform.fragments

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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import com.example.gamedevelopersplatform.entity.Game
import com.example.gamedevelopersplatform.util.GameDevelopersAppUtil
import com.example.gamedevelopersplatform.R
import com.example.gamedevelopersplatform.database.AppDatabase
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.Calendar

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
    private lateinit var gameId: String
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
    private var selectedImageUri: Uri? = null

    private lateinit var roomDatabase: AppDatabase


    companion object{
        fun newInstance(gameData: Game) = GamePageFragment().apply {
            arguments = bundleOf(
                "IMAGE" to gameData.image,
                "NAME" to gameData.name,
                "PRICE" to gameData.price,
                "RELEASE_DATE" to gameData.releaseDate,
                "DEVELOPER_ID" to gameData.developerId,
                "GAME_ID" to gameData.gameId
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
        gameId = arguments?.getString("GAME_ID", "").toString()
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

        roomDatabase = AppDatabase.getInstance(this.requireContext())
    }

    private fun addTextWatchers(){
        GameDevelopersAppUtil.handleTextChange(editNameInput) {
            GameDevelopersAppUtil.setTextAndHintTextColor(editNameInput, Color.WHITE)
        }
        GameDevelopersAppUtil.handleTextChange(editPriceInput) {
            GameDevelopersAppUtil.setTextAndHintTextColor(editPriceInput, Color.WHITE)
        }
    }

    private fun setButtonsOnClickEvent(){
        previewDeveloperProfileButton.setOnClickListener {
            loadDeveloperProfilePage()
        }
        previewEditButton.setOnClickListener {
            updateGamePageEditView()
            switchToEditLayout()
        }
        previewDeleteButton.setOnClickListener {
            //TODO - delete game from database and move user to myGames.
            deleteGame()
        }

        editPickADateButton.setOnClickListener {
            GameDevelopersAppUtil.showDatePicker(
                this.requireContext(),
                Calendar.getInstance()
            ) { formattedDate ->
                editReleaseDateView.text = formattedDate
            }
        }
        editChooseImageButton.setOnClickListener {
            GameDevelopersAppUtil.openGallery(galleryLauncher)
        }
        editSaveButton.setOnClickListener {
            updateGameDetails()
        }
        editCancelButton.setOnClickListener {
            switchToPreviewLayout()
        }
    }

    private fun updateGamePagePreviewView(){
        previewNameView.text = name
        previewPriceView.text = "$ " + price
        previewReleaseDateView.text = releaseDate
        GameDevelopersAppUtil.loadImageFromDB(
            storageRef, image,
            GameDevelopersAppUtil.GAMES_IMAGES_PATH, previewImageView
        )
        retrieveAndLoadDeveloperNickname()
    }

    private fun updateGamePageEditView(){
        editNameInput.setText(name)
        editPriceInput.setText(price)
        editReleaseDateView.text = releaseDate
        editImageView.setImageResource(R.drawable.place_holder_image)
    }

    private fun loadDeveloperProfilePage(){
        GameDevelopersAppUtil.changeFragmentFromFragment(
            requireActivity(), R.id.gamePageLayout, ProfilePageFragment.newInstance(developerId)
        )
    }

    //TODO - Make sure all details are filled Or In Update Query only update none empty fields.
    private fun updateGameDetails(){
        val newGameName: String = editNameInput.text.toString()
        val newGamePrice: String = editPriceInput.text.toString()
        val newGameReleaseDate: String = editReleaseDateView.text.toString()

        var imageUpdateStatus: Deferred<Pair<Boolean, String>>?
        var gameDetailsUpdateStatus: Deferred<Boolean>? = null
        val gameDetailsMap = hashMapOf<String, String>()
        var updateMessage: String = "Successfully Updated Game's : |";

        val nameValidation = nameRequireChangeValidation(newGameName)
        val priceValidation = priceRequireChangeValidation(newGamePrice)
        val releaseDateValidation = releaseDateRequireChangeValidation(newGameReleaseDate)

        var newImage = ""
        if(selectedImageUri!=null)
            newImage = GameDevelopersAppUtil.getImageNameFromUri(
                this.requireActivity().contentResolver, selectedImageUri!!
            )

        runBlocking {
            if(newImage != ""){
                imageUpdateStatus = async {
                    GameDevelopersAppUtil.uploadImageAndGetName(
                        storageRef, GameDevelopersAppUtil.GAMES_IMAGES_PATH, selectedImageUri!!
                    )
                }

                imageUpdateStatus?.await()?.let { result ->
                    if(result.first){
                        storageRef.child(
                            GameDevelopersAppUtil.GAMES_IMAGES_PATH
                                + image).delete()
                        gameDetailsMap["image"] = result.second
                        image = result.second
                        updateMessage += "Image|"
                    }else{
                        GameDevelopersAppUtil.setTextAndHintTextColor(
                            editChooseImageButton,
                            Color.RED
                        )
                        GameDevelopersAppUtil.popToast(
                            this@GamePageFragment.requireContext(),
                            "Failed uploading image",
                            Toast.LENGTH_SHORT
                        )
                        return@runBlocking
                    }
                }
            }

            if(nameValidation){
                gameDetailsMap["name"] = newGameName
                name = newGameName
            }

            if(priceValidation){
                gameDetailsMap["price"] = newGamePrice
                price = newGamePrice
            }

            if(releaseDateValidation){
                gameDetailsMap["releaseDate"] = newGameReleaseDate
                releaseDate = newGameReleaseDate
            }

            if(gameDetailsMap.isNotEmpty()) {
                gameDetailsMap["gameId"] = gameId
                gameDetailsUpdateStatus = async {
                    updateGameDetails(gameDetailsMap)
                }
            }

            val isDetailsUpdateSuccessful = gameDetailsUpdateStatus?.await() ?: true

            if(isDetailsUpdateSuccessful){
                //DB was updated successfully, Update local storage.
                val game: Game = GameDevelopersAppUtil.gameDataToEntity(gameDetailsMap)
                GameDevelopersAppUtil.updateGameDataInRoom(game, requireContext())

                updateGamePagePreviewView()
                switchToPreviewLayout()
            }
        }
    }

    private suspend fun updateGameDetails(updateDetailsMap: HashMap<String, String>): Boolean {
        return try {
            firestore.collection("games").document(gameId)
                .update(updateDetailsMap as Map<String, Any>).await()
            true
        } catch (e: Exception) { false }
    }

    private fun deleteGame(){

    }

    private fun nameRequireChangeValidation(newGameName: String): Boolean{
        if(newGameName.isEmpty() || newGameName == name) return false
        if(!GameDevelopersAppUtil.gameNameValidation(newGameName)){
            GameDevelopersAppUtil.popToast(
                this@GamePageFragment.requireContext(),
                "Name doesn't meet the requirements",
                Toast.LENGTH_SHORT
            )
            GameDevelopersAppUtil.setTextAndHintTextColor(editNameInput, Color.RED)
            return false
        }
        return true
    }

    private fun priceRequireChangeValidation(newGamePrice: String): Boolean{
        if(newGamePrice.isEmpty() || newGamePrice == price) return false
        if(!GameDevelopersAppUtil.gamePriceValidation(newGamePrice)){
            GameDevelopersAppUtil.popToast(
                this@GamePageFragment.requireContext(),
                "Price doesn't meet the requirements",
                Toast.LENGTH_SHORT
            )
            GameDevelopersAppUtil.setTextAndHintTextColor(editPriceInput, Color.RED)
            return false
        }
        return true
    }

    private fun releaseDateRequireChangeValidation(newReleaseDate: String): Boolean{
        return !(newReleaseDate.isEmpty() || newReleaseDate == releaseDate)
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

    private fun switchToEditLayout(){
        updateGamePageEditView()
        previewLayout.visibility = View.GONE
        editLayout.visibility = View.VISIBLE
    }

    private fun switchToPreviewLayout(){
        editLayout.visibility = View.GONE
        previewLayout.visibility = View.VISIBLE
    }
}