package com.omkar.chatapp.ui.chat.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.omkar.chatapp.ui.signin.signup.UserFirestore
import com.omkar.chatapp.utils.log

class DashboardRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getAllUsers(): LiveData<List<UserFirestore>> {
        val usersLiveData = MutableLiveData<List<UserFirestore>>()
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val users = result.toObjects(UserFirestore::class.java)
                    .filter { it.uid != auth.currentUser?.uid }  // Exclude the current logged-in user
                usersLiveData.postValue(users)
            }
            .addOnFailureListener { e ->
                log("UserRepo", "Error fetching users $e")
                usersLiveData.postValue(emptyList())
            }
        return usersLiveData
    }

    fun getUserData(): LiveData<UserFirestore> {
        val userData = MutableLiveData<UserFirestore>()

        db.collection("users").document(auth.currentUser?.uid.toString()).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    userData.value = documentSnapshot.toObject(UserFirestore::class.java)
                }
            }
            .addOnFailureListener { exception ->
                log("Firestore", "Error getting documents: $exception")
            }

        return userData
    }

    fun setUserOnlineStatus(isOnline: Boolean) {
        db.collection("users").document(auth.currentUser?.uid.toString())
            .update("isOnline", isOnline)
            .addOnSuccessListener {
                log("Firestore", "User online status updated to $isOnline")
            }
            .addOnFailureListener {
                e -> log("Firestore", "Error updating online status: $e")
            }
    }

}