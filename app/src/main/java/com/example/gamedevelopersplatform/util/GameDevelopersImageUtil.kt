package com.example.gamedevelopersplatform.util

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import com.example.gamedevelopersplatform.R
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.coroutines.tasks.await
import java.util.UUID

object GameDevelopersImageUtil {
    fun openGallery(galleryLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        galleryLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }

    fun uploadImageAndGetName(storageRef: StorageReference, path:String, imageUri: Uri,
                              onSuccess: (imageUrl: String) -> Unit, onFailure: (exception: Exception) -> Unit) {

        val imageName = UUID.randomUUID().toString()
        val imageReference = storageRef.child(path + imageName)
        val uploadTask = imageReference.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            onSuccess(imageName)
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    suspend fun uploadImageAndGetName(storageRef: StorageReference, path: String, imageUri: Uri): Pair<Boolean, String> {
        val imageName = UUID.randomUUID().toString()
        val imageReference = storageRef.child("$path$imageName")
        return try {
            val uploadTask = imageReference.putFile(imageUri).await()
            Pair(uploadTask.metadata != null, imageName)
        } catch (e: Exception) {
            Pair(false, imageName)
        }
    }

    private fun retrieveImageUrl(storageRef: StorageReference,
                                 imageName: String?, imagePath: String, onSuccess: (Uri) -> Unit){
        val imageReference = storageRef.child(imagePath + imageName)
        imageReference.downloadUrl.addOnSuccessListener { url ->
            onSuccess(url)
        }
    }

    fun loadImageFromDB(storageRef: StorageReference, imageName: String?, imagePath: String, imageView: ImageView){
        retrieveImageUrl(storageRef, imageName, imagePath){ imageUrl ->
            Picasso.get().load(imageUrl).placeholder(R.drawable.place_holder_image)
                .into(imageView)
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
}