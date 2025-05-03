package com.fashion.fashionapp.models

data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val profileImageUrl: String = "",
    val lastSeen: Long = 0
) 