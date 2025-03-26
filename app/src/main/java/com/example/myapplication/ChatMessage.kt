package com.example.myapplication

data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val isTyping: Boolean = false
)
