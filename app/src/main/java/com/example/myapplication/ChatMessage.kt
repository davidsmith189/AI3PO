package com.example.myapplication

import android.net.Uri

data class ChatMessage(
    val message: String? = null,
    val isUser: Boolean,
    val attachmentUri: String? = null
)
