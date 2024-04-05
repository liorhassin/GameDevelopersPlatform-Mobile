package com.example.gamedevelopersplatform

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
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
    private lateinit var editOldPassword: TextInputEditText
    private lateinit var editNewPassword: TextInputEditText


    //TODO - Add all text fields and inputs references.

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile_page, container, false)

        initializeParameters(view)
        addTextWatchers()
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
        editOldPassword = view.findViewById(R.id.profilePageEditOldPasswordInput)
        editNewPassword = view.findViewById(R.id.profilePageEditNewPasswordInput)
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
            saveUserDataChange()
        }
    }

    private fun addTextWatchers(){
        GameDevelopersAppUtil.handleTextChange(editNickname) {
            GameDevelopersAppUtil.setTextAndHintTextColor(editNickname, Color.WHITE)
        }
        GameDevelopersAppUtil.handleTextChange(editOldPassword) {
            GameDevelopersAppUtil.setTextAndHintTextColor(editOldPassword, Color.WHITE)
        }
        GameDevelopersAppUtil.handleTextChange(editNewPassword) {
            GameDevelopersAppUtil.setTextAndHintTextColor(editNewPassword, Color.WHITE)
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
        val newNickname = editNickname.text.toString()
        val oldPassword = editOldPassword.text.toString()
        val newPassword = editNewPassword.text.toString()
        val newBirthdate = editBirthdate.text.toString()

        val (nicknameValidation, passwordValidation, birthdateValidation) =
            saveUserInputsValidation(newNickname, newPassword, newBirthdate)

        if(oldPassword.isNotEmpty()){
            reAuthenticate(oldPassword) {
                if(passwordValidation && newPassword.isNotEmpty()) {
                    updateUserPassword(newPassword) {
                        editOldPassword.setText("")
                        editNewPassword.setText("")
                    }
                }
                else{
                    GameDevelopersAppUtil.setTextAndHintTextColor(editOldPassword, Color.RED)
                }
            }
        }

        if(nicknameValidation || birthdateValidation) {
            updateUserData(newNickname, newBirthdate) {
                editNickname.setText(newNickname)
            }
        }

        if(!nicknameValidation)
            GameDevelopersAppUtil.setTextAndHintTextColor(editNickname, Color.RED)
        if(!passwordValidation)
            GameDevelopersAppUtil.setTextAndHintTextColor(editNewPassword, Color.RED)
    }

    private fun reAuthenticate(currentPassword: String, onSuccess: () -> Unit){
        val user = firebaseAuth.currentUser
        val email = user?.email

        if(email != null && currentPassword.isNotEmpty()){
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener {exception ->
                Toast.makeText(this.context,
                    "Re-Authentication failed, exception: $exception",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserData(newNickname: String, newBirthdate: String, onSuccess: () -> Unit){
        val oldNickname = userData.nickname
        val oldBirthdate = userData.birthDate
        val userId = firebaseAuth.currentUser?.uid.toString()

        val userUpdates = hashMapOf<String, String>()

        if(oldNickname != newNickname && newNickname.isNotEmpty()) userUpdates["nickname"] = newNickname
        if(oldBirthdate != newBirthdate && newBirthdate.isNotEmpty()) userUpdates["birthDate"] = newBirthdate

        if(userUpdates.isNotEmpty()){
            firestore.collection("users").document(userId)
                .update(userUpdates as Map<String, String>).addOnSuccessListener {
                    Toast.makeText(this.context,
                        "User data updated successfully",
                        Toast.LENGTH_SHORT).show()
                    onSuccess()
                }.addOnFailureListener { exception ->
                    Toast.makeText(this.context,
                        "Failed to update user data, exception: $exception",
                        Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateUserPassword(newPassword: String, onSuccess: () -> Unit){
        firebaseAuth.currentUser?.updatePassword(newPassword)?.addOnSuccessListener {
            Toast.makeText(this.context,
                "Password was changed successfully",
                Toast.LENGTH_SHORT).show()
            onSuccess()
        }?.addOnFailureListener {
            Toast.makeText(this.context,
                "Failed to change password",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserInputsValidation(newNickname: String ,newPassword: String, newBirthdate: String): Triple<Boolean, Boolean, Boolean>{
        return Triple(
            (GameDevelopersAppUtil.nicknameValidation(newNickname) || newNickname.isEmpty()),
            (GameDevelopersAppUtil.passwordValidation(newPassword)) || newPassword.isEmpty(),
            newBirthdate.isNotEmpty())
    }

}