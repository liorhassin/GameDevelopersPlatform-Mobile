package com.example.gamedevelopersplatform

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Calendar

//TODO - Refactor profile page code, split to methods and outsource parameters.
class ProfilePageFragment : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: StorageReference

    private lateinit var connectedUserId: String
    private lateinit var userData: UserData

    private lateinit var previewLayout: ConstraintLayout
    private lateinit var editLayout: ConstraintLayout

    private lateinit var myGamesButton: Button
    private lateinit var editProfileButton: Button
    private lateinit var cancelEditButton: Button
    private lateinit var saveEditButton: Button
    private lateinit var showDatePickerButton: Button

    private lateinit var previewNickname: TextView
    private lateinit var previewImage: CircleImageView
    private lateinit var previewEmail: TextView
    private lateinit var previewBirthdate: TextView
    private lateinit var previewGamesCount: TextView

    private lateinit var editNickname: TextInputEditText
    private lateinit var editImage: CircleImageView
    private lateinit var editEmail: TextView
    private lateinit var editBirthdate: TextView
    private lateinit var editPassword: TextInputEditText
    private lateinit var editConfirmPassword: TextInputEditText


    //TODO - Add all text fields and inputs references.

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile_page, container, false)

        initializeParameters(view)
        setButtonsOnClickEvent()
        fetchUserData { updateProfilePageData() }

        return view
    }

    private fun initializeParameters(view: View){
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        connectedUserId = firebaseAuth.currentUser?.uid.toString()

        previewLayout = view.findViewById(R.id.profilePagePreviewLayout)
        editLayout = view.findViewById(R.id.profilePageEditLayout)

        myGamesButton = view.findViewById<Button>(R.id.profilePagePreviewMyGamesButton)
        editProfileButton = view.findViewById<Button>(R.id.profilePagePreviewEditProfileButton)
        cancelEditButton = view.findViewById<Button>(R.id.profilePageEditCancelButton)
        saveEditButton = view.findViewById<Button>(R.id.profilePageEditSaveButton)
        showDatePickerButton = view.findViewById<Button>(R.id.profilePageEditPickADateButton)

        previewNickname = view.findViewById(R.id.profilePagePreviewNickname)
        previewImage = view.findViewById(R.id.profilePagePreviewCircleImage)
        previewEmail = view.findViewById(R.id.profilePagePreviewEmailText)
        previewBirthdate = view.findViewById(R.id.profilePagePreviewBirthdateText)
        previewGamesCount = view.findViewById(R.id.profilePagePreviewGamesCountText)

        editNickname = view.findViewById(R.id.profilePageEditNicknameInput)
        editImage = view.findViewById(R.id.profilePageEditCircleImage)
        editEmail = view.findViewById(R.id.profilePageEditEmailText)
        editBirthdate = view.findViewById(R.id.profilePageEditBirthdateText)
        editPassword = view.findViewById(R.id.profilePageEditPasswordInput)
        editConfirmPassword = view.findViewById(R.id.profilePageEditConfirmPasswordInput)
    }

    private fun setButtonsOnClickEvent(){
        myGamesButton.setOnClickListener{
            GameDevelopersAppUtil.changeFragmentFromFragment(requireActivity(),
                R.id.profilePagePreviewLayout, MyGamesPageFragment.newInstance(connectedUserId))
        }

        editProfileButton.setOnClickListener {
            previewLayout.visibility = View.GONE
            editLayout.visibility = View.VISIBLE
        }


        cancelEditButton.setOnClickListener {
            editLayout.visibility = View.GONE
            previewLayout.visibility = View.VISIBLE
        }

        showDatePickerButton.setOnClickListener {
            GameDevelopersAppUtil.showDatePicker(requireContext(), Calendar.getInstance()) { formattedDate ->
                previewBirthdate.text = formattedDate
                editBirthdate.text = formattedDate
            }
        }

        saveEditButton.setOnClickListener {
            //TODO - Complete save loop with helper function.
            saveUserDataChange()
        }
    }

    private fun fetchUserData(onSuccess: () -> Unit){
        firestore.collection("users").document(connectedUserId).get()
            .addOnSuccessListener { userDocument ->
                val userDataObject = userDocument.toObject<UserData>()
                if(userDataObject != null) userData = userDataObject
        }.addOnCompleteListener{
            if(it.isSuccessful) onSuccess()
        }
    }

    private fun updateProfilePageData(){
        previewNickname.text = userData.nickname
        Picasso.get().load(userData.profileImage).placeholder(R.drawable.place_holder_image)
            .into(previewImage)
        previewEmail.text = userData.email
        previewBirthdate.text = userData.birthDate
        previewGamesCount.text = userData.userGames.size.toString()

        editNickname.setText(userData.nickname)
        Picasso.get().load(userData.profileImage).placeholder(R.drawable.place_holder_image)
            .into(editImage)
        editEmail.text = userData.email
        editBirthdate.text = userData.birthDate
    }

    private fun saveUserDataChange(){
        //1) Validations - Check if all inputs are correct, Empty inputs are ignored(details remained unchanged)
        //2) Update DB - Send update to firebase after all validations have passed, Change only inserted Inputs.
        //3) Update if DB saving worked or failed.
        //4) Move User - Send user back to his profile page with all the details updated, No need to call back from DB.
    }

    private fun saveUserInputsValidation(){

    }

}