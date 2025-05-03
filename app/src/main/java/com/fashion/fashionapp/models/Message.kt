package com.fashion.fashionapp.models

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val isRead: Boolean = false
) 