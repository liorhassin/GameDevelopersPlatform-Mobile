package com.example.gamedevelopersplatform

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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

}