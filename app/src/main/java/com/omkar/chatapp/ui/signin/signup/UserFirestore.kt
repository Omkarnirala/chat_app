package com.omkar.chatapp.ui.signin.signup

data class UserFirestore(
    var uid: String = "",
    var email: String? = null,
    var displayName: String? = null,
    var profileImageUrl: String? = null,
    var status: String? = "Hey there! I am using ChatApp.",
    var lastMessage: String? = null,
    var lastOnlineTime: Long = System.currentTimeMillis(),
    var isOnline: Boolean = false
)
