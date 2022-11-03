package com.app.tests.domain.repository

import com.app.tests.data.model.ApiResult
import com.app.tests.data.model.LoginDto
import com.app.tests.data.model.RegistrationDto
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {

    suspend fun signUpWithEmail(data: RegistrationDto): ApiResult<FirebaseUser?>

    suspend fun loginWithEmail(data: LoginDto): ApiResult<FirebaseUser?>

    suspend fun getCurrentUser(): ApiResult<FirebaseUser?>
}