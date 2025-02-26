package com.example.myapplication

data class ChatMessage(
    val message: String,
    val isUser: Boolean // true for user, false for chatbot
)
