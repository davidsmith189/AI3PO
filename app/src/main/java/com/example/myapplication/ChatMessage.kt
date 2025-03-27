package com.example.myapplication

import android.net.Uri
import com.google.firebase.Timestamp

data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val isTyping: Boolean = false,
    val attachmentUri: String = "",
    val timestamp: com.google.firebase.Timestamp? = null
)
