package com.example.gamedevelopersplatform

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddGamePageFragment : Fragment() {
    //TODO - organize parameters to the correct functions after everything works remove redundant parameters.
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var calendar = Calendar.getInstance()
    private lateinit var releaseDateText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_game_page, container, false)

        val pickADateButton = view.findViewById<Button>(R.id.addGameShowDatePickerButton)
        releaseDateText = view.findViewById(R.id.addGamePageReleaseDateText)

        pickADateButton.setOnClickListener {
            showDatePicker()
        }
        return view
    }

    private fun showDatePicker(){
        val datePickerDialog = DatePickerDialog(requireContext(), {_, year:Int, monthOfYear:Int, dayOfYear:Int ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, monthOfYear, dayOfYear)
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate.time)
            releaseDateText.text = formattedDate
        },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}