package com.example.myapplication.model

data class Conversation(
    val userName: String,
    val lastMessage: String,
    val timestamp: Long,
    val profileImage: String
)