package com.example.myapplication.model

data class Conversation(
    var userName: String = "",
    var userId: String = "",
    var lastMessage: String = "",
    var timestamp: Long = 0L,
    var profileImage: String = ""
) {
    // Firebase yêu cầu một constructor mặc định (không tham số)
    constructor() : this("", "", "",0L, "")
}
