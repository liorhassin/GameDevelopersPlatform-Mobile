package com.example.gamedevelopersplatform

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AddGamePageFragment : Fragment() {
    //TODO - organize parameters to the correct functions after everything works remove redundant parameters.
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_game_page, container, false)

        val calendar: Calendar = Calendar.getInstance()
        val releaseDateText: TextView = view.findViewById(R.id.addGamePageReleaseDateText)

        val pickADateButton = view.findViewById<Button>(R.id.addGameShowDatePickerButton)
        pickADateButton.setOnClickListener {
            GameDevelopersAppUtil.showDatePicker(requireContext(), calendar){formattedDate ->
                releaseDateText.text = formattedDate
            }
        }
        return view
    }
}