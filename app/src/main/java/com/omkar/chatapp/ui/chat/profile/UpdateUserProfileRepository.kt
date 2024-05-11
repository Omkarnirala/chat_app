package com.omkar.chatapp.ui.chat.profile

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.omkar.chatapp.utils.log
import java.util.UUID

class UpdateUserProfileRepository {

    val mTag = "UpdateUserProfileRepository"
    private val db = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference

    fun updateUserProfile(updates: Map<String, Any>, callback: (Boolean) -> Unit) {
        db.collection("users").document(auth.currentUser?.uid.toString())
            .update(updates)
            .addOnSuccessListener {
                log(mTag, "User profile successfully updated!")
                callback(true)
            }
            .addOnFailureListener {
                e -> log(mTag, "Error updating user profile: $e")
                callback(false)
            }
    }

    fun uploadImage(
        imageUri: Uri?,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val imageName = auth.currentUser?.email
        val imageRef = storageRef.child("$imageName/${"Profile Pic"}")
        imageRef.putFile(imageUri?: Uri.EMPTY)
            .addOnSuccessListener {
                // Image uploaded successfully
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                // Handle unsuccessful uploads
                onFailure(exception)
            }
    }

}