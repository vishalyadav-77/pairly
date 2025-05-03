package com.fashion.fashionapp.models

data class Chat(
    val id: String = "",
    val participants: List<String> = listOf(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val lastMessageSenderId: String = ""
) 