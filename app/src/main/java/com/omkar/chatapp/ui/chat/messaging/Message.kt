package com.omkar.chatapp.ui.chat.messaging

data class Message(
    val text: String,
    val time: Long,
    val person: String
)