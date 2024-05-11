package com.omkar.chatapp.ui.chat.messaging

import com.google.firebase.Timestamp

data class ChatMessageModel(
    var message: String? = "",
    var senderID: String? = "",
    var timeStamps: Timestamp? = null,
)
