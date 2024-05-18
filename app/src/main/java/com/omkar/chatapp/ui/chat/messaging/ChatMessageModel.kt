package com.omkar.chatapp.ui.chat.messaging

import androidx.annotation.Keep
import com.google.firebase.Timestamp

@Keep
data class ChatMessageModel(
    var message: String? = "",
    var senderID: String? = "",
    var timeStamps: Timestamp? = null,
)
