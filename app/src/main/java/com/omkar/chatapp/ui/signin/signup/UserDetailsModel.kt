package com.omkar.chatapp.ui.signin.signup

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserDetailsModel(
    var uid: String = "",
    var email: String? = null,
    var displayName: String? = null,
    var profileImageUrl: String? = null,
    var status: String? = "Hey there! I am using ChatApp.",
    var lastMessage: String? = null,
    var lastOnlineTime: Timestamp = Timestamp.now(),
    var isOnline: Boolean? = null
) : Parcelable

