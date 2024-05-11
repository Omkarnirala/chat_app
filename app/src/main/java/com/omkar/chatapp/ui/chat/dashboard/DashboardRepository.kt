package com.omkar.chatapp.ui.chat.dashboard

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.omkar.chatapp.ui.signin.signup.UserDetailsModel
import com.omkar.chatapp.utils.log

class DashboardRepository {

    private val mTag: String = "DashboardRepository"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getUserData(): MutableLiveData<UserDetailsModel?> {
        val userData = MutableLiveData<UserDetailsModel?>()
        db.collection("users").document(auth.currentUser?.uid.toString())
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
                log(mTag, "getUserData: ${snapshot?.data}")
                try {
                    userData.value = snapshot?.toObject(UserDetailsModel::class.java)
                } catch (e: Exception) {
                    log(mTag, "getUserData Exception: $e")
                }
            }

        return userData
    }

    fun setUserOnlineStatus(isOnline: Boolean) {
        db.collection("users").document(auth.currentUser?.uid.toString())
            .update(
                mapOf(
                    "isOnline" to isOnline,
                    "lastOnlineTime" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener {
                log("Firestore", "User online status updated to $isOnline")
            }
            .addOnFailureListener { e ->
                log("Firestore", "Error updating online status: $e")
            }
    }

}