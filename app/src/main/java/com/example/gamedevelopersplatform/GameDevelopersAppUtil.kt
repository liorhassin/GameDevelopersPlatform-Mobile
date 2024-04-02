package com.example.gamedevelopersplatform

import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

//TODO - Split Util into ImageUtil, GeneralUtil, Maybe more..
object GameDevelopersAppUtil {
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

    fun <T> setTextAndHintTextColor(view: T,color:Int) where T : TextView{
        if (view.text != null) view.setTextColor(color)
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

    fun uploadImageAndGetUrl(contentResolver: ContentResolver, storageRef: StorageReference, path:String, imageUri: Uri,
        onSuccess: (imageUrl: String) -> Unit, onFailure: (exception: Exception) -> Unit) {

        val imageName = UUID.randomUUID().toString()
        val imageReference = storageRef.child(path + imageName)
        val uploadTask = imageReference.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            imageReference.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                onSuccess(imageUrl)
            }.addOnFailureListener { exception ->
                onFailure(exception)
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
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

    fun changeFragmentFromFragment(transaction: FragmentActivity, currentLayoutId: Int, newFragment: Fragment){
        val fragmentTransaction = transaction.supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(currentLayoutId, newFragment)
        fragmentTransaction.commit()
    }

}