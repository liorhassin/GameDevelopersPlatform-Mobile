package com.example.gamedevelopersplatform

import android.app.DatePickerDialog
import android.content.Context
import android.widget.TextView
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
}