package com.app.testik.data.model

data class RegistrationDto(
    val email: String,
    val password: String?,
    val username: String,
    val avatar: String
)