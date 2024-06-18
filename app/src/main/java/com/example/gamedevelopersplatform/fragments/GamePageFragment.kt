package com.example.gamedevelopersplatform.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.Global
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import com.example.gamedevelopersplatform.entity.Game
import com.example.gamedevelopersplatform.util.GameDevelopersAppUtil
import com.example.gamedevelopersplatform.R
import com.example.gamedevelopersplatform.dao.CurrencyDao
import com.example.gamedevelopersplatform.database.AppDatabase
import com.example.gamedevelopersplatform.entity.Currency
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.net.URL
import java.util.Calendar
import java.util.HashSet

class GamePageFragment : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: StorageReference

    private val baseCurrency: String = "USD"
    private lateinit var currencies: ArrayList<Currency>
    private lateinit var currenciesHashmap: HashMap<String,String>
    private lateinit var supportedCurrencies: HashSet<String>

    private lateinit var connectedUserId: String
    private lateinit var image: String
    private lateinit var name: String
    private lateinit var price: String
    private lateinit var releaseDate: String
    private lateinit var developerId: String
    private lateinit var gameId: String
    private var developerName: String = "Unknown"
    private var isOwner = false

    //Preview Mode
    private lateinit var previewLayout: ConstraintLayout

    private lateinit var previewSpinnerView: Spinner
    private lateinit var previewExchangeView: TextView
    private lateinit var previewNameView: TextView
    private lateinit var previewPriceView: TextView
    private lateinit var previewReleaseDateView: TextView
    private lateinit var previewImageView: ShapeableImageView
    private lateinit var previewDeveloperNameView: TextView
    private lateinit var previewDeveloperProfileButton: Button
    private lateinit var previewEditButton: Button
    private lateinit var previewDeleteButton: Button


    //Edit Mode
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
        getCurrencyRates()
        spinnerSetup()
        updateGamePagePreviewView()

        return view
    }

    private fun initializeParameters(view: View) {
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        currencies = arrayListOf()
        currenciesHashmap = HashMap()
        supportedCurrencies = HashSet()
        supportedCurrencies.add("ILS")
        supportedCurrencies.add("EUR")
        supportedCurrencies.add("GBP")

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

        previewSpinnerView = view.findViewById(R.id.gamePagePreviewSpinner)
        previewExchangeView = view.findViewById(R.id.gamePagePreviewExchangeText)
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
            deleteGame()
            GameDevelopersAppUtil.changeFragmentFromFragment(requireActivity(),
                R.id.gamePageLayout, MyGamesPageFragment.newInstance(connectedUserId))
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
                updateGamePagePreviewView()
                switchToPreviewLayout()
            }
        }
    }

    private suspend fun updateGameDetails(gameDetailsMap: HashMap<String, String>): Boolean {
        return try {

            firestore.collection("games").document(gameId)
                .update(gameDetailsMap as Map<String, Any>).await()

            val game: Game = GameDevelopersAppUtil.gameDataToEntity(gameDetailsMap)
            GameDevelopersAppUtil.updateGameDataInRoom(game, requireContext())

            true
        } catch (e: Exception) { false }
    }

    private fun deleteGame(){
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("games").document(gameId)
            .delete()
            .addOnSuccessListener {
                GameDevelopersAppUtil.deleteGameDataInRoom(gameId, this.requireContext())
            }
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

    @OptIn(DelicateCoroutinesApi::class)
    private fun getCurrencyRates(){
        //TODO - add if request fails pull from firebase, if firebase fails pull from room.
        val apiRequestKey = "https://v6.exchangerate-api.com/v6/aea9dcadf0c12acfc7213f75/latest/$baseCurrency"
        GlobalScope.launch(Dispatchers.IO) {
            try{
                val apiResult = URL(apiRequestKey).readText()
                val jsonObject = JSONObject(apiResult)

                val conversionRates = jsonObject.getJSONObject("conversion_rates")
                //TODO - Add fetch currency rate from firebase and if failed from room cache.(should be added to spinner not here).

                currenciesJsonToHashmap(conversionRates)
                saveCurrenciesToFirebase()

            }catch (e: Exception){
                Log.e("error", "$e")
            }
        }
    }

    private fun currenciesJsonToHashmap(currenciesJson: JSONObject){
        currenciesJson.keys().forEach { key ->
            if(key in supportedCurrencies) {
                val rate = currenciesJson.getString(key)
                currenciesHashmap[key] = rate
            }
        }
    }

    private fun saveCurrenciesToFirebase(){
        currenciesHashmap.forEach { (key, value) ->
            val currencyMap: HashMap<String, String> = HashMap()
            currencyMap["currencyName"] = key
            currencyMap["currencyRate"] = value
            saveCurrencyToFirebase(currencyMap)
        }
    }

    private fun saveCurrencyToFirebase(currencyData: HashMap<String, String>){
        val currencyName = currencyData["currencyName"]
        if (currencyName != null) {
            firestore.collection("currencies").document(currencyName).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        firestore.collection("currencies").document(currencyName)
                            .set(currencyData, SetOptions.merge())
                            .addOnSuccessListener {
                                currencyData["currencyId"] = document.id
                                generateCurrencyEntityAndSaveLocally(currencyData)
                            }
                    } else {
                        firestore.collection("currencies").document(currencyName).set(currencyData)
                            .addOnSuccessListener {
                                currencyData["currencyId"] = currencyName
                                generateCurrencyEntityAndSaveLocally(currencyData)
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("saveCurrencyToFirebase", "Error getting document", exception)
                }
        }
    }

    private fun generateCurrencyEntityAndSaveLocally(currencyData: HashMap<String, String>){
        val currency = Currency(currencyData["currencyId"]!!,
            currencyData["currencyName"],
            currencyData["currencyRate"])
        saveCurrencyToRoomDB(currency, this@GamePageFragment.requireContext())
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun saveCurrencyToRoomDB(currency: Currency, context: Context){
        val roomDB: AppDatabase = AppDatabase.getInstance(context)
        val currencyDao: CurrencyDao = roomDB.currencyDao()

        GlobalScope.launch(Dispatchers.IO) {
            currencyDao.insert(currency)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun getCurrencyRate(currencyKey: String, onSuccess: (rate: String) -> Unit){
        firestore.collection("currencies").document(currencyKey).get()
            .addOnSuccessListener { document ->
                val rate = document.get("currencyRate").toString()
                onSuccess(rate)
            }.addOnFailureListener {
                GlobalScope.launch(Dispatchers.IO) {
                    onSuccess(roomDatabase.currencyDao().getRateByName(currencyKey))
                }
            }
    }

    private fun spinnerSetup(){
        ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.currencies,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            previewSpinnerView.adapter = adapter
        }

        previewSpinnerView.onItemSelectedListener = (object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                getCurrencyRate(parent?.getItemAtPosition(position).toString()) { rate ->
                    val text = (price.toFloat() * rate.toFloat()).toString()
                    previewExchangeView.text = text
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        })
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