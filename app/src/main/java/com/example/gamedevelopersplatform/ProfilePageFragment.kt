package com.example.gamedevelopersplatform

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Calendar

class ProfilePageFragment : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: StorageReference

    private lateinit var connectedUserId: String
    private lateinit var userData: UserData
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null

    private lateinit var previewLayout: ConstraintLayout
    private lateinit var editLayout: ConstraintLayout

    private lateinit var myGamesButton: Button
    private lateinit var editProfileButton: Button
    private lateinit var cancelEditButton: Button
    private lateinit var saveEditButton: Button
    private lateinit var showDatePickerButton: Button
    private lateinit var chooseImageButton: Button

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile_page, container, false)

        initializeParameters(view)
        addTextWatchers()
        setButtonsOnClickEvent()
        fetchUserData { updateProfilePagePreviewView() }

        return view
    }

    private fun initializeParameters(view: View){
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        connectedUserId = firebaseAuth.currentUser?.uid.toString()

        previewLayout = view.findViewById(R.id.profilePagePreviewLayout)
        editLayout = view.findViewById(R.id.profilePageEditLayout)
        galleryLauncher = generateGalleryLauncher {
                data -> handleSelectedImage(data)
            if(data != null) GameDevelopersAppUtil.setTextAndHintTextColor(chooseImageButton, Color.WHITE)
        }

        myGamesButton = view.findViewById<Button>(R.id.profilePagePreviewMyGamesButton)
        editProfileButton = view.findViewById<Button>(R.id.profilePagePreviewEditProfileButton)
        cancelEditButton = view.findViewById<Button>(R.id.profilePageEditCancelButton)
        saveEditButton = view.findViewById<Button>(R.id.profilePageEditSaveButton)
        showDatePickerButton = view.findViewById<Button>(R.id.profilePageEditPickADateButton)
        chooseImageButton = view.findViewById<Button>(R.id.profilePageEditChooseImageButton)

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
            updateProfilePageEditView()
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

        chooseImageButton.setOnClickListener {
            GameDevelopersAppUtil.openGallery(galleryLauncher)
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

    private fun updateProfilePagePreviewView(){
        previewNickname.text = userData.nickname
        GameDevelopersAppUtil.loadImageFromDB(storageRef, userData.profileImage,
            GameDevelopersAppUtil.USERS_PROFILE_IMAGES_PATH, previewImage)
        previewEmail.text = userData.email
        previewBirthdate.text = userData.birthDate
        previewGamesCount.text = userData.userGames.size.toString()
    }

    private fun updateProfilePageEditView(){
        editNickname.setText(userData.nickname)
        editEmail.text = userData.email
        editBirthdate.text = userData.birthDate
    }

    private fun saveUserDataChange(){
        //TODO - each update function returns a task, after all tasks are completed move to profile page preview.
        val userId = firebaseAuth.currentUser?.uid.toString()
        val oldNickname = userData.nickname
        val newNickname = editNickname.text.toString()
        val oldPassword = editOldPassword.text.toString()
        val newPassword = editNewPassword.text.toString()
        val oldBirthdate = userData.birthDate
        val newBirthdate = editBirthdate.text.toString()

        var newImage = ""
        if(selectedImageUri != null)
            newImage = GameDevelopersAppUtil.getImageNameFromUri(this.requireActivity().contentResolver, selectedImageUri!!)

        val (nicknameValidation, passwordValidation, birthdateValidation, imageValidation) =
            saveUserInputsValidation(newNickname, newPassword, newBirthdate, newImage)

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

        if(imageValidation){
            GameDevelopersAppUtil.uploadImageAndGetName(storageRef,
                GameDevelopersAppUtil.USERS_PROFILE_IMAGES_PATH,
                selectedImageUri!!, { imageName->
                    Log.e("imageName", imageName)
                    updateUserImage(imageName, userId)
                },{
                    Toast.makeText(this.context,
                        "Failed to upload new Image",
                        Toast.LENGTH_SHORT).show()
                })
        }

        if(nicknameValidation)
            updateUserNickname(oldNickname, newNickname, userId)
        else
            GameDevelopersAppUtil.setTextAndHintTextColor(editNickname, Color.RED)

        if(birthdateValidation)
            updateUserBirthdate(oldBirthdate, newBirthdate, userId)

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
                    "Re-Authentication failed, exception: ${exception.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserImage(newImage: String, userId: String){
        val userUpdates = hashMapOf<String, String>()
        if(newImage!="") userUpdates["profileImage"] = newImage

        if(userUpdates.isNotEmpty()){
            storageRef.child(GameDevelopersAppUtil.USERS_PROFILE_IMAGES_PATH + userData.profileImage).delete().addOnCompleteListener {
                firestore.collection("users").document(userId)
                    .update(userUpdates as Map<String, String>).addOnSuccessListener {
                        userData.profileImage = newImage
                    }.addOnFailureListener { exception ->
                        Toast.makeText(this.context,
                            "Failed to update profile image, exception: $exception",
                            Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener { exception ->
                Toast.makeText(this.context,
                    "Failed to delete previous image Update failed, exception: $exception",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserNickname(oldNickname: String, newNickname: String, userId: String){
        val userUpdates = hashMapOf<String, String>()
        if(oldNickname != newNickname && newNickname.isNotEmpty()) userUpdates["nickname"] = newNickname

        if(userUpdates.isNotEmpty()){
            firestore.collection("users").document(userId)
                .update(userUpdates as Map<String, String>).addOnSuccessListener {
                    userData.nickname = newNickname
                }.addOnFailureListener { exception ->
                    Toast.makeText(this.context,
                        "Failed to update nickname, exception: $exception",
                        Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateUserBirthdate(oldBirthdate: String, newBirthdate: String, userId: String){
        val userUpdates = hashMapOf<String, String>()
        if(oldBirthdate != newBirthdate && newBirthdate.isNotEmpty()) userUpdates["birthDate"] = newBirthdate

        if(userUpdates.isNotEmpty()){
            firestore.collection("users").document(userId)
                .update(userUpdates as Map<String, String>).addOnSuccessListener {
                    userData.birthDate = newBirthdate
                }.addOnFailureListener { exception ->
                    Toast.makeText(this.context,
                        "Failed to update birthdate, exception: $exception",
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

    private fun saveUserInputsValidation(newNickname: String ,newPassword: String, newBirthdate: String, newImage: String): GameDevelopersAppUtil.QuadrupleBooleans{
        return GameDevelopersAppUtil.QuadrupleBooleans(
            (GameDevelopersAppUtil.nicknameValidation(newNickname) || newNickname.isEmpty()),
            (GameDevelopersAppUtil.passwordValidation(newPassword)) || newPassword.isEmpty(),
            newBirthdate.isNotEmpty(),
            newImage != "")
    }

    //TODO - Make this function generic in util
    private fun generateGalleryLauncher(callback: (Intent?)->Unit): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                callback(data)
            }
        }
    }

    //TODO - Make this function generic in util
    private fun handleSelectedImage(data: Intent?) {
        data?.data?.let { uri ->
            selectedImageUri = uri
            editImage.setImageURI(uri)
        }
    }

}