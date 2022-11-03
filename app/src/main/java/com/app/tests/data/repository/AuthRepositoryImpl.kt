package com.app.tests.data.repository

import com.app.tests.data.model.ApiResult
import com.app.tests.data.model.RegistrationDto
import com.app.tests.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun signUpWithEmail(data: RegistrationDto): ApiResult<FirebaseUser?> {
        return try {
            with (firebaseAuth) {
                createUserWithEmailAndPassword(data.email, data.password).await()
                currentUser?.updateProfile(
                    userProfileChangeRequest {
                        displayName = data.username
                    }
                )?.await()
                ApiResult.Success(currentUser)
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun getCurrentUser(): ApiResult<FirebaseUser?> {
        return try {
            ApiResult.Success(firebaseAuth.currentUser)
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }
}