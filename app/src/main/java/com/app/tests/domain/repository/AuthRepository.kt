package com.app.tests.domain.repository

import com.app.tests.data.model.ApiResult
import com.app.tests.data.model.LoginDto
import com.app.tests.data.model.RegistrationDto
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {

    suspend fun signUpWithEmail(data: RegistrationDto): ApiResult<Unit>

    suspend fun loginWithEmail(data: LoginDto): ApiResult<Unit>

    suspend fun loginWithGoogle(credential: AuthCredential): ApiResult<Boolean?>

    suspend fun getCurrentUser(): FirebaseUser?
}