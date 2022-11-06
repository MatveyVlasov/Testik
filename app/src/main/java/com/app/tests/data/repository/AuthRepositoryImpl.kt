package com.app.tests.data.repository

import com.app.tests.data.model.ApiResult
import com.app.tests.data.model.LoginDto
import com.app.tests.data.model.RegistrationDto
import com.app.tests.domain.repository.AuthRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun signUpWithEmail(data: RegistrationDto): ApiResult<Unit> {

        return try {
            with (firebaseAuth) {
                if (data.password == null) return ApiResult.Error("No password provided")

                createUserWithEmailAndPassword(data.email, data.password).await()
                currentUser?.updateProfile(
                    userProfileChangeRequest {
                        displayName = data.username
                    }
                )?.await()
                ApiResult.Success()
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun loginWithEmail(data: LoginDto): ApiResult<Unit> {
        return try {
            with (firebaseAuth) {
                signInWithEmailAndPassword(data.email, data.password).await()
                ApiResult.Success()
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun loginWithGoogle(credential: AuthCredential): ApiResult<Boolean?> {
        return try {
            with (firebaseAuth) {
                var isNewUser: Boolean?

                signInWithCredential(credential).also { task ->
                    task.await()
                    isNewUser = task.result.additionalUserInfo?.isNewUser
                }

                ApiResult.Success(isNewUser)
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    override suspend fun deleteCurrentUser(): ApiResult<Unit> {
        return try {
            firebaseAuth.currentUser?.delete()
            ApiResult.Success()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun resetPassword(email: String): ApiResult<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            ApiResult.Success()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }
}