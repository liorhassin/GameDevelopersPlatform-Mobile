package com.example.gamedevelopersplatform

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

//TODO - Refactor profile page code, split to methods and outsource parameters.
class ProfilePageFragment : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var connectedUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile_page, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        connectedUserId = firebaseAuth.currentUser?.uid.toString()

        val previewLayout: ConstraintLayout = view.findViewById(R.id.profilePagePreviewLayout)
        val editLayout: ConstraintLayout = view.findViewById(R.id.profilePageEditLayout)
        val birthdateTextView: TextView = view.findViewById(R.id.profilePagePreviewBirthdateText)
        val birthdateEditTextView: TextView = view.findViewById(R.id.profilePageEditBirthdateInput)


        val myGamesButton = view.findViewById<Button>(R.id.profilePagePreviewMyGamesButton)
        myGamesButton.setOnClickListener{
            GameDevelopersAppUtil.changeFragmentFromFragment(requireActivity(),
                R.id.profilePagePreviewLayout, MyGamesPageFragment.newInstance(connectedUserId))
        }

        val editProfileButton = view.findViewById<Button>(R.id.profilePagePreviewEditProfileButton)
        editProfileButton.setOnClickListener {
            previewLayout.visibility = View.GONE
            editLayout.visibility = View.VISIBLE
        }

        val cancelEditButton = view.findViewById<Button>(R.id.profilePageEditCancelButton)
        cancelEditButton.setOnClickListener {
            editLayout.visibility = View.GONE
            previewLayout.visibility = View.VISIBLE
        }

        val saveEditButton = view.findViewById<Button>(R.id.profilePageEditSaveButton)
        saveEditButton.setOnClickListener {
            //Button Loop:
            //1) Validations - Check if all inputs are correct, Empty inputs are ignored(details remained unchanged)
            //2) Update DB - Send update to firebase after all validations have passed, Change only inserted Inputs.
            //3) Update if DB saving worked or failed.
            //4) Move User - Send user back to his profile page with all the details updated, No need to call back from DB.

        }

        val showDatePickerButton = view.findViewById<Button>(R.id.profilePageEditPickADateButton)
        showDatePickerButton.setOnClickListener {
            GameDevelopersAppUtil.showDatePicker(requireContext(), Calendar.getInstance()) { formattedDate ->
                birthdateTextView.text = formattedDate
                birthdateEditTextView.text = formattedDate
            }
        }

        return view
    }
}