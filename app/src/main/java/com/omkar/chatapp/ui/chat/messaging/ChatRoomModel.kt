package com.omkar.chatapp.ui.chat.messaging

import com.google.firebase.Timestamp

data class ChatRoomModel(
    var chatroomId: String? = null,
    var userIds: List<String?>? = null,
    var lastMessageTimestamp: Timestamp? = null,
    var lastMessageSenderId: String? = null,
    var lastMessage: String? = null,
)
