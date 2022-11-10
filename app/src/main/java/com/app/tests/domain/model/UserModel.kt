package com.app.tests.domain.model

data class UserModel(
    val email: String,
    val username: String,
    val firstName: String = "",
    val lastName: String = "",
    val avatar: String
)