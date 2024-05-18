package com.omkar.chatapp.ui.chat.messaging

import androidx.annotation.Keep

@Keep
data class Message(
    val text: String,
    val time: Long,
    val person: String
)