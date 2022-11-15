package com.app.testik.data.repository

import android.content.Context
import com.app.testik.data.model.ApiResult
import com.app.testik.data.model.RegistrationDto
import com.app.testik.data.model.UserDto
import com.app.testik.domain.repository.FirestoreRepository
import com.app.testik.util.execute
import com.app.testik.util.isOnline
import com.app.testik.util.loadedFromServer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseFirestore: FirebaseFirestore
): FirestoreRepository {

    override suspend fun addUser(data: RegistrationDto): ApiResult<Unit> {
        return try {
            with(data) {
                val userData = mapOf(
                    "email" to email,
                    "username" to username,
                    "avatar" to avatar
                )
                firebaseFirestore.collection("users").whereEqualTo("username", username).get()
                    .also {
                        it.await()
                        if (!it.result.isEmpty) return ApiResult.Error("Username already taken")
                    }
                firebaseFirestore.collection("users").document(email).set(userData).execute()
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun getUserInfo(email: String?, source: Source): ApiResult<UserDto> {
        try {
            if (email == null) return ApiResult.Error("No email provided")

            firebaseFirestore.collection("users").document(email).get(source).also {
                it.await()
                return if (it.isSuccessful) {
                    val user = it.result.toObject(UserDto::class.java) ?: return ApiResult.Error("No data found")
                    ApiResult.Success(user)
                }
                else ApiResult.Error(it.exception?.message)
            }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    override suspend fun updateUser(data: UserDto): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        return try {
            with(data) {
                firebaseFirestore.collection("users").whereEqualTo("username", username).get().also {
                    it.await()
                    if (!it.result.isEmpty && it.result.documents.first().data?.get("email") != email) return ApiResult.Error("Username already taken")
                }
                val newData = mutableMapOf(
                    "email" to email,
                    "username" to username,
                    "firstName" to firstName,
                    "lastName" to lastName
                )
                if (avatar.loadedFromServer() || avatar.isEmpty()) newData["avatar"] = avatar
                firebaseFirestore.collection("users").document(email).set(newData).execute()
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun deleteUser(email: String?): ApiResult<Unit> {
        if (email == null) return ApiResult.Error("No email provided")
        if (!isOnline(context)) return ApiResult.NoInternetError()

        return try {
            firebaseFirestore.collection("users").document(email).delete().execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }
}