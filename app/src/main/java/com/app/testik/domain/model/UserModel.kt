package com.app.testik.domain.model

data class UserModel(
    val uid: String = "",
    val email: String,
    val username: String,
    val firstName: String = "",
    val lastName: String = "",
    val avatar: String
)