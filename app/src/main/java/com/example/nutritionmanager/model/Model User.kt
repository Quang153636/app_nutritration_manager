package com.example.nutritionmanager.model

data class User(
    val id: Int = 0,
    val username: String,
    val email: String,
    val password: String, // Trong thực tế nên mã hóa, ở đây demo đơn giản
    val fullName: String = ""
)