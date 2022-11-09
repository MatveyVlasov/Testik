package com.app.tests.domain.model

data class RegistrationModel(
    val email: String,
    val password: String? = null,
    val username: String,
    val avatar: String = ""
)