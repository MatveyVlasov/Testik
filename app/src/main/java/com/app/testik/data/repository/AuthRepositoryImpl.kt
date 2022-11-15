package com.app.testik.data.repository

import com.app.testik.data.model.ApiResult
import com.app.testik.data.model.LoginDto
import com.app.testik.data.model.RegistrationDto
import com.app.testik.domain.repository.AuthRepository
import com.app.testik.util.execute
import com.google.firebase.auth.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun signUpWithEmail(data: RegistrationDto): ApiResult<Unit> {
        if (data.password == null) return ApiResult.Error("No password provided")

        return try {
            firebaseAuth.createUserWithEmailAndPassword(data.email, data.password).execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun loginWithEmail(data: LoginDto): ApiResult<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(data.email, data.password).execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun loginWithGoogle(credential: AuthCredential): ApiResult<Boolean?> {
        return try {
            var isNewUser: Boolean?

            firebaseAuth.signInWithCredential(credential).also { task ->
                task.await()
                isNewUser = task.result.additionalUserInfo?.isNewUser
            }

            ApiResult.Success(isNewUser)
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    override suspend fun signOut(): ApiResult<Unit> {
        return try {
            firebaseAuth.signOut()
            ApiResult.Success()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun deleteCurrentUser(): ApiResult<Unit> {
        return try {
            firebaseAuth.currentUser!!.delete().execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): ApiResult<Unit> {
        return try {
            reauthenticate(oldPassword).also {
                if (it is ApiResult.Error) return it
            }
            firebaseAuth.currentUser!!.updatePassword(newPassword).execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun resetPassword(email: String): ApiResult<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    private suspend fun reauthenticate(password: String): ApiResult<Unit> {
        return try {
            firebaseAuth.currentUser?.let { user ->
                user.reauthenticate(EmailAuthProvider.getCredential(user.email!!, password)).execute()
            } ?: ApiResult.Error("An error occurred")
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }
}