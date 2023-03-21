package com.app.testik.data.repository

import android.content.Context
import com.app.testik.data.model.ApiResult
import com.app.testik.data.model.LoginDto
import com.app.testik.data.model.RegistrationDto
import com.app.testik.domain.repository.AuthRepository
import com.app.testik.util.execute
import com.app.testik.util.isOnline
import com.google.firebase.auth.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun signUpWithEmail(data: RegistrationDto): ApiResult<FirebaseUser?> {
        if (data.password == null) return ApiResult.Error("No password provided")
        if (!isOnline(context)) return ApiResult.NoInternetError()

        try {
            firebaseAuth.createUserWithEmailAndPassword(data.email, data.password).also {
                it.await()
                return ApiResult.Success(it.result.user)
            }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    override suspend fun loginWithEmail(data: LoginDto): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        return try {
            firebaseAuth.signInWithEmailAndPassword(data.email, data.password).execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun loginWithGoogle(credential: AuthCredential): ApiResult<AuthResult> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        try {
            firebaseAuth.signInWithCredential(credential).also {
                it.await()
                return ApiResult.Success(it.result)
            }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
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
        if (!isOnline(context)) return ApiResult.NoInternetError()

        return try {
            firebaseAuth.currentUser!!.delete().execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        return try {
            reauthenticate(oldPassword).also {
                if (it is ApiResult.Error) return it
            }
            firebaseAuth.currentUser!!.updatePassword(newPassword).execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun resetPassword(email: String, language: String): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        return try {
            firebaseAuth.setLanguageCode(language)
            firebaseAuth.sendPasswordResetEmail(email).execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    private suspend fun reauthenticate(password: String): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        return try {
            firebaseAuth.currentUser?.let { user ->
                user.reauthenticate(EmailAuthProvider.getCredential(user.email!!, password)).execute()
            } ?: ApiResult.Error("An error occurred")
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }
}