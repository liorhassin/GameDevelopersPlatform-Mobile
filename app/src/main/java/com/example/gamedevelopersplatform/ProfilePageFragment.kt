package com.example.gamedevelopersplatform

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
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
    private lateinit var changePasswordLayout: ConstraintLayout

    private lateinit var myGamesButton: Button
    private lateinit var editProfileDetailsButton: Button
    private lateinit var cancelDetailsEditButton: Button
    private lateinit var saveDetailsEditButton: Button
    private lateinit var showDatePickerButton: Button
    private lateinit var chooseImageButton: Button
    private lateinit var changePasswordButton: Button
    private lateinit var cancelChangePasswordButton: Button
    private lateinit var saveChangePasswordButton: Button

    private lateinit var previewNickname: TextView
    private lateinit var previewImage: CircleImageView
    private lateinit var previewEmail: TextView
    private lateinit var previewBirthdate: TextView
    private lateinit var previewGamesCount: TextView

    private lateinit var editNickname: TextInputEditText
    private lateinit var editImage: CircleImageView
    private lateinit var editEmail: TextView
    private lateinit var editBirthdate: TextView

    private lateinit var changePasswordOldPassword: TextInputEditText
    private lateinit var changePasswordNewPassword: TextInputEditText
    private lateinit var changePasswordConfirmPassword: TextInputEditText

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
        changePasswordLayout = view.findViewById(R.id.profilePageChangePasswordLayout)

        galleryLauncher = generateGalleryLauncher {
                data -> handleSelectedImage(data)
            if(data != null) GameDevelopersAppUtil.setTextAndHintTextColor(chooseImageButton, Color.WHITE)
        }

        myGamesButton = view.findViewById<Button>(R.id.profilePagePreviewMyGamesButton)
        editProfileDetailsButton = view.findViewById<Button>(R.id.profilePagePreviewEditProfileButton)
        cancelDetailsEditButton = view.findViewById<Button>(R.id.profilePageEditCancelButton)
        saveDetailsEditButton = view.findViewById<Button>(R.id.profilePageEditSaveButton)
        showDatePickerButton = view.findViewById<Button>(R.id.profilePageEditPickADateButton)
        chooseImageButton = view.findViewById<Button>(R.id.profilePageEditChooseImageButton)
        changePasswordButton = view.findViewById<Button>(R.id.profilePagePreviewChangePassword)
        cancelChangePasswordButton = view.findViewById<Button>(R.id.profilePageChangePasswordCancelButton)
        saveChangePasswordButton = view.findViewById<Button>(R.id.profilePageChangePasswordSaveButton)

        previewNickname = view.findViewById(R.id.profilePagePreviewNickname)
        previewImage = view.findViewById(R.id.profilePagePreviewCircleImage)
        previewEmail = view.findViewById(R.id.profilePagePreviewEmailText)
        previewBirthdate = view.findViewById(R.id.profilePagePreviewBirthdateText)
        previewGamesCount = view.findViewById(R.id.profilePagePreviewGamesCountText)

        editNickname = view.findViewById(R.id.profilePageEditNicknameInput)
        editImage = view.findViewById(R.id.profilePageEditCircleImage)
        editEmail = view.findViewById(R.id.profilePageEditEmailText)
        editBirthdate = view.findViewById(R.id.profilePageEditBirthdateText)

        changePasswordOldPassword = view.findViewById(R.id.profilePageChangePasswordOldPasswordInput)
        changePasswordNewPassword = view.findViewById(R.id.profilePageChangePasswordNewPasswordInput)
        changePasswordConfirmPassword = view.findViewById(R.id.profilePageChangePasswordConfirmPasswordInput)
    }

    private fun setButtonsOnClickEvent(){
        myGamesButton.setOnClickListener{
            GameDevelopersAppUtil.changeFragmentFromFragment(requireActivity(),
                R.id.profilePagePreviewLayout, MyGamesPageFragment.newInstance(connectedUserId))
        }

        changePasswordButton.setOnClickListener {
            switchToChangePasswordLayout()
        }

        cancelChangePasswordButton.setOnClickListener {
            clearChangePasswordInputs()
            switchToPreviewLayout()
        }

        saveChangePasswordButton.setOnClickListener {
            reAuthenticateAndUpdatePassword()
        }

        editProfileDetailsButton.setOnClickListener {
            updateProfilePageEditView()
            switchToEditLayout()
        }

        cancelDetailsEditButton.setOnClickListener {
            switchToPreviewLayout()
        }

        showDatePickerButton.setOnClickListener {
            GameDevelopersAppUtil.showDatePicker(requireContext(), Calendar.getInstance()) { formattedDate ->
                previewBirthdate.text = formattedDate
                editBirthdate.text = formattedDate
            }
        }

        saveDetailsEditButton.setOnClickListener {
            updateUserDetails()
        }

        chooseImageButton.setOnClickListener {
            GameDevelopersAppUtil.openGallery(galleryLauncher)
        }

    }

    private fun addTextWatchers(){
        //TODO - Fix Hint Color changing to white also, Find a way to change Hint color back to "hint_color".
        GameDevelopersAppUtil.handleTextChange(editNickname) {
            GameDevelopersAppUtil.setTextAndHintTextColor(editNickname, Color.WHITE)
        }
        GameDevelopersAppUtil.handleTextChange(changePasswordOldPassword) {
            GameDevelopersAppUtil.setTextAndHintTextColor(changePasswordOldPassword, Color.WHITE)
        }
        GameDevelopersAppUtil.handleTextChange(changePasswordNewPassword) {
            GameDevelopersAppUtil.setTextAndHintTextColor(changePasswordNewPassword, Color.WHITE)
        }
        GameDevelopersAppUtil.handleTextChange(changePasswordConfirmPassword) {
            GameDevelopersAppUtil.setTextAndHintTextColor(changePasswordConfirmPassword, Color.WHITE)
        }
        GameDevelopersAppUtil.handleTextChange(chooseImageButton) {
            GameDevelopersAppUtil.setTextAndHintTextColor(chooseImageButton, Color.WHITE)
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
        editImage.setImageResource(R.drawable.place_holder_image)
        editNickname.setText(userData.nickname)
        editEmail.text = userData.email
        editBirthdate.text = userData.birthDate
    }

    private fun reAuthenticateAndUpdatePassword(){
        val user = firebaseAuth.currentUser
        val email = user?.email
        val oldPassword: String = changePasswordOldPassword.text.toString()
        val newPassword: String = changePasswordNewPassword.text.toString()
        val confirmPassword: String = changePasswordConfirmPassword.text.toString()

        if(!validatePasswordInputs(oldPassword, newPassword, confirmPassword)) return

        if(email != null){
            val credential = EmailAuthProvider.getCredential(email, oldPassword)
            user.reauthenticate(credential).addOnSuccessListener {
                firebaseAuth.currentUser?.updatePassword(newPassword)?.addOnSuccessListener {
                    clearChangePasswordInputs()
                    switchToPreviewLayout()
                    GameDevelopersAppUtil.popToast(this@ProfilePageFragment.requireContext()
                        , "Successfully updated user password"
                        , Toast.LENGTH_SHORT)
                }?.addOnFailureListener { exception ->
                    GameDevelopersAppUtil.popToast(this@ProfilePageFragment.requireContext()
                        , "Failed to update password, exception: ${exception.message}"
                        , Toast.LENGTH_SHORT)
                }
            }.addOnFailureListener {exception ->

                GameDevelopersAppUtil.popToast(this@ProfilePageFragment.requireContext()
                    , "Failed to re-authenticate, exception: ${exception.message}"
                    , Toast.LENGTH_SHORT)
            }
        }
    }

    private fun validatePasswordInputs(oldPassword:String, newPassword:String, confirmPassword:String): Boolean{
        if(oldPassword.isEmpty()) {
            GameDevelopersAppUtil.setTextAndHintTextColor(changePasswordOldPassword, Color.RED)
            GameDevelopersAppUtil.popToast(this@ProfilePageFragment.requireContext()
                , "Please enter your old password", Toast.LENGTH_SHORT)
            return false
        }

        if(newPassword.isEmpty() || !GameDevelopersAppUtil.passwordValidation(newPassword)){
            GameDevelopersAppUtil.setTextAndHintTextColor(changePasswordNewPassword, Color.RED)
            GameDevelopersAppUtil.popToast(this@ProfilePageFragment.requireContext()
                , "New Password doesn't meet the password requirements", Toast.LENGTH_SHORT)
            return false
        }

        if(confirmPassword.isEmpty() || confirmPassword!=newPassword){
            GameDevelopersAppUtil.setTextAndHintTextColor(changePasswordConfirmPassword, Color.RED)
            GameDevelopersAppUtil.popToast(this@ProfilePageFragment.requireContext()
                , "Confirm Password is empty, Or is not equal to New Password", Toast.LENGTH_SHORT)
            return false
        }
        return true
    }

    private fun updateUserDetails(){
        val updateDetailsMap = hashMapOf<String,String>()
        var imageUpdateStatus: Deferred<Pair<Boolean, String>>? = null
        var detailsUpdateStatus: Deferred<Boolean>? = null
        var updateMessage: String = "Successfully Updated : |";

        val oldNickname = userData.nickname
        val newNickname = editNickname.text.toString()
        val oldBirthdate = userData.birthDate
        val newBirthdate = editBirthdate.text.toString()

        var newImage = ""
        if(selectedImageUri!=null)
            newImage = GameDevelopersAppUtil.getImageNameFromUri(
                this.requireActivity().contentResolver, selectedImageUri!!)

        val (nicknameValidation, birthdateValidation, imageValidation) =
            userDetailsValidation(newNickname, oldNickname, newBirthdate, oldBirthdate, newImage)

        runBlocking {
            if(imageValidation) {
                imageUpdateStatus = async { GameDevelopersAppUtil.uploadImageAndGetNameTest(
                    storageRef, GameDevelopersAppUtil.USERS_PROFILE_IMAGES_PATH, selectedImageUri!!)}

                imageUpdateStatus?.await()?.let { result ->
                    if(result.first){
                        storageRef.child(GameDevelopersAppUtil.USERS_PROFILE_IMAGES_PATH
                                + userData.profileImage).delete()
                        updateDetailsMap["profileImage"] = result.second
                        userData.profileImage = result.second
                        updateMessage += "Image|"
                    }else{
                        GameDevelopersAppUtil.setTextAndHintTextColor(chooseImageButton, Color.RED)
                        GameDevelopersAppUtil.popToast(this@ProfilePageFragment.requireContext()
                            , "Failed uploading image", Toast.LENGTH_SHORT)
                        return@runBlocking
                    }
                }
            }

            if(nicknameValidation) {
                updateDetailsMap["nickname"] = newNickname
                userData.nickname = newNickname
                updateMessage += "Nickname|"
            }

            if(birthdateValidation) {
                updateDetailsMap["birthDate"] = newBirthdate
                userData.birthDate = newBirthdate
                updateMessage += "Birthdate|"
            }

            if(updateDetailsMap.isNotEmpty())
                detailsUpdateStatus = async { updateUserDetails(updateDetailsMap) }

            val isImageUpdateSuccessful = imageUpdateStatus?.await()?.first ?: true
            val isDetailsUpdateSuccessful = detailsUpdateStatus?.await() ?: true

            if(isDetailsUpdateSuccessful && isImageUpdateSuccessful){
                updateProfilePagePreviewView()
                switchToPreviewLayout()
                if(updateMessage!="Successfully Updated : |")
                    GameDevelopersAppUtil.popToast(this@ProfilePageFragment.requireContext(),
                        updateMessage, Toast.LENGTH_SHORT);
            }
        }

    }

    private suspend fun updateUserDetails(updateDetailsMap: HashMap<String, String>): Boolean {
        return try {
            firestore.collection("users").document(connectedUserId)
                .update(updateDetailsMap as Map<String, Any>).await()
            true
        } catch (e: Exception) { false }
    }

    private fun userDetailsValidation(
        newNickname: String, oldNickname: String,
        newBirthdate: String, oldBirthdate: String,
        newImage: String):Triple<Boolean, Boolean, Boolean>{

        return Triple(
            (newNickname.isNotEmpty() && newNickname!=oldNickname && GameDevelopersAppUtil.nicknameValidation(newNickname)),
            (newBirthdate.isNotEmpty() && newBirthdate!=oldBirthdate),
            (newImage != "")
        )
    }

    private fun generateGalleryLauncher(callback: (Intent?)->Unit): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                callback(data)
            }
        }
    }

    private fun handleSelectedImage(data: Intent?) {
        data?.data?.let { uri ->
            selectedImageUri = uri
            editImage.setImageURI(uri)
        }
    }

    private fun switchToPreviewLayout(){
        editLayout.visibility = View.GONE
        changePasswordLayout.visibility = View.GONE
        previewLayout.visibility = View.VISIBLE
    }

    private fun switchToEditLayout(){
        changePasswordLayout.visibility = View.GONE
        previewLayout.visibility = View.GONE
        editLayout.visibility = View.VISIBLE
    }

    private fun switchToChangePasswordLayout(){
        previewLayout.visibility = View.GONE
        editLayout.visibility = View.GONE
        changePasswordLayout.visibility = View.VISIBLE
    }

    private fun clearChangePasswordInputs(){
        changePasswordOldPassword.setText("")
        changePasswordNewPassword.setText("")
        changePasswordConfirmPassword.setText("")
    }

}