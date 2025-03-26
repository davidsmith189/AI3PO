package com.example.myapplication

import android.net.Uri
import com.google.firebase.Timestamp

data class ChatMessage(
    val message: String? = null,
    val isUser: Boolean,
    val attachmentUri: String? = null,
    val timestamp: com.google.firebase.Timestamp? = null
)
