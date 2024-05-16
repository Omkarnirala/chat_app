package com.omkar.chatapp.utils

import androidx.core.app.NotificationCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Locale

object FirebaseUtil {

    private val mTag = "FirebaseUtil"

    fun currentUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid
    fun currentUserEmailId(): String? = FirebaseAuth.getInstance().currentUser?.email

    val notificationBuilders = mutableMapOf<String, NotificationCompat.Builder>()

    fun isLoggedIn(): Boolean = currentUserId() != null

    fun currentUserDetails(): DocumentReference =
        FirebaseFirestore.getInstance().collection("users").document(currentUserId().toString())

    fun currentUserName(uid: String?): String {
        var currentUserName = ""
        FirebaseFirestore.getInstance().collection("users")
            .document(uid.toString())
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val document = it.result
                    if (document != null) {
                        currentUserName =  document.getString("displayName").toString()
                        log(mTag, "currentUserName DocumentSnapshot data: $currentUserName")
                    }
                }
            }
            .addOnFailureListener {
                log(mTag, "currentUserName Error getting document: $it")
            }
        return currentUserName
    }

    fun getAllUserDetails(): CollectionReference = FirebaseFirestore.getInstance().collection("users")

    fun updateUserStatus(userId: String?, isOnline: Boolean) {
        val db = FirebaseFirestore.getInstance()
        val userStatus = hashMapOf("isOnline" to isOnline, "lastOnlineTime" to Timestamp.now())

        userId?.let {
            db.collection("users").document(it)
                .set(userStatus, SetOptions.merge())
                .addOnSuccessListener {
                    notificationBuilders.clear()
                    log(mTag, "DocumentSnapshot successfully updated with isOnline: $isOnline")
                }
                .addOnFailureListener { e -> log(mTag, "Error updating document with isOnline: $e") }
        }
    }

    fun updateUserToken(userId: String?, token: String){
        //"token" to FirebaseMessaging.getInstance().token
        val db = FirebaseFirestore.getInstance()
        val userStatus = hashMapOf("token" to token)

        userId?.let {
            db.collection("users").document(it)
                .set(userStatus, SetOptions.merge())
                .addOnSuccessListener { log(mTag, "DocumentSnapshot successfully updated with token: $token") }
                .addOnFailureListener { e -> log(mTag, "Error updating document with isOnline: $e") }
        }
    }

    fun getOtherUserOnlineStatus(uid: String?): DocumentReference = FirebaseFirestore.getInstance().collection("users").document(uid.toString())

    fun allUserCollectionReference(): CollectionReference =
        FirebaseFirestore.getInstance().collection("users")

    fun getChatroomReference(chatroomId: String): DocumentReference =
        FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId)

    fun getChatroomMessageReference(chatroomId: String): CollectionReference =
        getChatroomReference(chatroomId).collection("chats")

    fun getChatroomId(userId1: String, userId2: String): String =
        if (userId1.hashCode() < userId2.hashCode()) "$userId1$userId2" else "$userId2$userId1"

    fun allChatroomCollectionReference(): CollectionReference =
        FirebaseFirestore.getInstance().collection("chatrooms")

    fun getOtherUserFromChatroom(userIds: List<String?>?): DocumentReference =
        if (userIds?.get(0) == currentUserId()) {
            allUserCollectionReference().document(userIds?.get(1).toString())
        } else {
            allUserCollectionReference().document(userIds?.get(0).toString())
        }

    fun timestampToString(timestamp: Timestamp): String =
        SimpleDateFormat("HH:mm a", Locale.getDefault()).format(timestamp.toDate())

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    fun getCurrentProfilePicStorageRef(): StorageReference =
        FirebaseStorage.getInstance().getReference("${currentUserEmailId()}/${"Profile Pic"}")

    fun getOtherProfilePicStorageRef(otherUserId: String): StorageReference =
        FirebaseStorage.getInstance().getReference("$otherUserId/${"Profile Pic"}")


}


