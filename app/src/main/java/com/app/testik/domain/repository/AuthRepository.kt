package com.app.testik.domain.repository

import com.app.testik.data.model.ApiResult
import com.app.testik.data.model.LoginDto
import com.app.testik.data.model.RegistrationDto
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {

    suspend fun signUpWithEmail(data: RegistrationDto): ApiResult<Unit>

    suspend fun loginWithEmail(data: LoginDto): ApiResult<Unit>

    suspend fun loginWithGoogle(credential: AuthCredential): ApiResult<Boolean?>

    suspend fun getCurrentUser(): FirebaseUser?

    suspend fun signOut(): ApiResult<Unit>

    suspend fun deleteCurrentUser(): ApiResult<Unit>

    suspend fun changePassword(oldPassword: String, newPassword: String): ApiResult<Unit>

    suspend fun resetPassword(email: String): ApiResult<Unit>
}