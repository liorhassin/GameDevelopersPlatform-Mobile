package com.example.gamedevelopersplatform.util

import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.gamedevelopersplatform.entity.Game
import com.example.gamedevelopersplatform.adapters.GamesAdapter
import com.example.gamedevelopersplatform.R
import com.example.gamedevelopersplatform.dao.GameDao
import com.example.gamedevelopersplatform.database.AppDatabase
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

//TODO - Split Util into ImageUtil, GeneralUtil, Maybe more..
object GameDevelopersAppUtil {
    data class QuadrupleBooleans(val first: Boolean, val second: Boolean,
                                 val third: Boolean, val fourth: Boolean)

    const val USERS_PROFILE_IMAGES_PATH = "UsersProfileImages/"
    const val GAMES_IMAGES_PATH = "GamesImages/"

    fun showDatePicker(context: Context, calendar: Calendar, callback:(String) -> Unit){
        val datePickerDialog = DatePickerDialog(context, {_, year:Int, monthOfYear:Int, dayOfYear:Int ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, monthOfYear, dayOfYear)
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            callback(dateFormat.format(selectedDate.time))
        },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    //TODO - Change how this function works to support hint changing back to another color or default.
    fun <T> setTextAndHintTextColor(view: T,color:Int) where T : TextView{
        if(view.text != null) view.setTextColor(color)
        if(view.hint != null) view.setHintTextColor(color)
    }

    fun <T> handleTextChange(view: T,successFunction: () -> Unit) where T : TextView {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Not required for this implementation
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Not required for this implementation
            }
            override fun afterTextChanged(p0: Editable?) {
                if(view.text != null && view.currentTextColor != Color.WHITE)
                    successFunction()
                if(view.hint != null && view.currentHintTextColor != Color.WHITE)
                    successFunction()
            }
        }
        view.addTextChangedListener(textWatcher)
    }

    fun openGallery(galleryLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        galleryLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }

    fun uploadImageAndGetName(storageRef: StorageReference, path:String, imageUri: Uri,
        onSuccess: (imageUrl: String) -> Unit, onFailure: (exception: Exception) -> Unit) {

        val imageName = UUID.randomUUID().toString()
        val imageReference = storageRef.child(path + imageName)
        val uploadTask = imageReference.putFile(imageUri)

        uploadTask.addOnSuccessListener {
                onSuccess(imageName)
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    //Attempt:{ Rename if works before deleting comment
    suspend fun uploadImageAndGetName(storageRef: StorageReference, path: String, imageUri: Uri): Pair<Boolean, String> {
        val imageName = UUID.randomUUID().toString()
        val imageReference = storageRef.child("$path$imageName")
        return try {
            val uploadTask = imageReference.putFile(imageUri).await()
            Pair(uploadTask.metadata != null, imageName)
        } catch (e: Exception) {
            Pair(false, imageName)
        }
    }

    private fun retrieveImageUrl(storageRef: StorageReference,
                                 imageName: String?, imagePath: String, onSuccess: (Uri) -> Unit){
        val imageReference = storageRef.child(imagePath + imageName)
        imageReference.downloadUrl.addOnSuccessListener { url ->
            onSuccess(url)
        }
    }

    fun loadImageFromDB(storageRef: StorageReference, imageName: String?, imagePath: String, imageView: ImageView){
        retrieveImageUrl(storageRef, imageName, imagePath){ imageUrl ->
            Picasso.get().load(imageUrl).placeholder(R.drawable.place_holder_image)
                .into(imageView)
        }
    }

    fun getImageNameFromUri(contentResolver: ContentResolver, uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    return it.getString(nameIndex)
                }
            }
        }
        return UUID.randomUUID().toString() // fallback to UUID if name retrieval fails
    }


    fun changeFragmentFromFragment(transaction: FragmentActivity,
                                   currentLayoutId: Int, newFragment: Fragment){
        val fragmentTransaction = transaction.supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(currentLayoutId, newFragment)
        fragmentTransaction.commit()
    }

    fun populateRecyclerView(recyclerView: RecyclerView, gamesList: ArrayList<Game>,
                             storageRef: StorageReference, fragmentActivity: FragmentActivity, currentLayoutId: Int){
        recyclerView.adapter = GamesAdapter(gamesList, storageRef, fragmentActivity, currentLayoutId)
    }

    fun nicknameValidation(nickname: String): Boolean {
        //Nickname contains Numbers,Alphabet and white spaces.
        //Requirements: Length between 2-16.
        val nicknameRegex = Regex("^(?=.*[A-Za-z].*[A-Za-z])[A-Za-z0-9_ ]{2,16}\$")
        return nicknameRegex.matches(nickname)
    }

    fun passwordValidation(password: String): Boolean {
        //Password contains Numbers, Alphabet or special characters.
        //Requirements: 1 Capital letter, 1 normal letter, 1 number and length between 6-36.
        val passwordRegex = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!])(?=\\S+\$).{6,36}\$")
        return passwordRegex.matches(password)
    }

    fun emailValidation(email: String): Boolean {
        //Email contains all the standard characters allowed for email addresses.
        //Requirements: Starts with at least one Character followed by '@',
        //and '.' followed by at least two Characters.
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$")
        return emailRegex.matches(email)
    }

    fun gamePriceValidation(price: String): Boolean{
        //Price contains numbers with a possible dot for decimal numbers.
        //Requirements: Price between 0-300, with maximum of two numbers after the dot.
        val priceRegex = Regex("^(?:\\d{1,2}|1\\d{2}|300)(?:\\.\\d{1,2})?\$")
        return priceRegex.matches(price)
    }

    fun gameNameValidation(name: String): Boolean{
        //Name contains Numbers, Alphabet or white space.
        //Requirements: Length between 2-30.
        val nameRegex = Regex("^(?=.*[A-Za-z].*[A-Za-z])[A-Za-z0-9_' ]{2,30}\$")
        return nameRegex.matches(name)
    }

    fun popToast(context: Context, message: String, duration: Int){
        Toast.makeText(context, message, duration).show()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun saveGamesToRoom(games: List<Game>, context: Context){
        val roomDB: AppDatabase = AppDatabase.getInstance(context)
        val gameDao: GameDao = roomDB.gameDao()

        GlobalScope.launch(Dispatchers.IO) {
            games.forEach { game ->
                gameDao.insert(game)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun saveGameToRoom(game: Game, context: Context){
        val roomDB: AppDatabase = AppDatabase.getInstance(context)
        val gameDao: GameDao = roomDB.gameDao()

        GlobalScope.launch(Dispatchers.IO) {
            gameDao.insert(game)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun updateGameDataInRoom(game: Game, context: Context){
        val roomDB: AppDatabase = AppDatabase.getInstance(context)
        val gameDao: GameDao = roomDB.gameDao()

        GlobalScope.launch(Dispatchers.IO) {
            gameDao.update(game)
        }
    }

    fun gameDataToEntity(gameData: HashMap<String, String>): Game{
        return Game(
            name = gameData["name"],
            developerId = gameData["developerId"],
            price = gameData["price"],
            gameId = gameData["gameId"]!!,
            releaseDate = gameData["releaseDate"],
            image = gameData["image"]
        )
    }

}