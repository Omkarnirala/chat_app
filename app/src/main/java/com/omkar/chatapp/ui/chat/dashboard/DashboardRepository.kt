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

    fun getNewAllUsers(): LiveData<List<UserFirestore>> {
        val usersLiveData = MutableLiveData<List<UserFirestore>>()
        db.collection("users")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    log("UserRepo", "Listen failed $e")
                    usersLiveData.postValue(emptyList())
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { it.toObject(UserFirestore::class.java) }
                    ?.filter { it.uid != FirebaseAuth.getInstance().currentUser?.uid }
                usersLiveData.postValue(users ?: emptyList())
            }
        return usersLiveData
    }

}