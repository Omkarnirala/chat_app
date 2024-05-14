package com.omkar.chatapp.ui.chat.messaging

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatRoomModel(
    var chatroomId: String? = null,
    var userIds: List<String?>? = null,
    var lastMessageTimestamp: Timestamp? = null,
    var lastMessageSenderId: String? = null,
    var lastMessage: String? = null,
) : Parcelable