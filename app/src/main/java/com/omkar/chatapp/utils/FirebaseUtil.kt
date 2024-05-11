package com.omkar.chatapp.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Locale

object FirebaseUtil {

    fun currentUserId(): String? = FirebaseAuth.getInstance().uid

    fun isLoggedIn(): Boolean = currentUserId() != null

    fun currentUserDetails(): DocumentReference =
        FirebaseFirestore.getInstance().collection("users").document(currentUserId()!!)

    fun getAllUserDetails(): CollectionReference = FirebaseFirestore.getInstance().collection("users")

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

    fun getOtherUserFromChatroom(userIds: List<String>): DocumentReference =
        if (userIds[0] == currentUserId()) {
            allUserCollectionReference().document(userIds[1])
        } else {
            allUserCollectionReference().document(userIds[0])
        }

    fun timestampToString(timestamp: com.google.firebase.Timestamp): String =
        SimpleDateFormat("HH:mm a", Locale.getDefault()).format(timestamp.toDate())

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    fun getCurrentProfilePicStorageRef(): StorageReference =
        FirebaseStorage.getInstance().getReference("profile_pic/${currentUserId()}")

    fun getOtherProfilePicStorageRef(otherUserId: String): StorageReference =
        FirebaseStorage.getInstance().getReference("$otherUserId/${"Profile Pic"}")

}