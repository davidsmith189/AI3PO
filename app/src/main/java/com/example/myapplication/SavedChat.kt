package com.example.myapplication

data class SavedChat (
        val title: String = "",
        val lastMessage: String = "",
        val messages: List<ChatMessage> = emptyList()
)
