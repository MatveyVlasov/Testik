package com.app.testik.data.repository

import android.content.Context
import com.app.testik.data.model.ApiResult
import com.app.testik.data.model.RegistrationDto
import com.app.testik.data.model.UserDto
import com.app.testik.domain.repository.UserRepository
import com.app.testik.util.execute
import com.app.testik.util.isOnline
import com.app.testik.util.loadedFromServer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseFirestore: FirebaseFirestore
): UserRepository {

    private val collection
        get() = firebaseFirestore.collection(COLLECTION_ID)

    override suspend fun addUser(data: RegistrationDto, uid: String?): ApiResult<Unit> {
        if (uid == null) return ApiResult.Error("No user id provided")

        return try {
            with(data) {
                val userData = mapOf(
                    "email" to email,
                    "username" to username,
                    "avatar" to avatar
                )
                collection.whereEqualTo("username", username).get()
                    .also {
                        it.await()
                        if (!it.result.isEmpty) return ApiResult.Error("Username already taken")
                    }
                collection.document(uid).set(userData).execute()
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun getUserInfo(uid: String?, source: Source): ApiResult<UserDto> {
        if (uid == null) return ApiResult.Error("No user id provided")

        try {
            collection.document(uid).get(source).also {
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

    override suspend fun updateUser(data: UserDto, uid: String?): ApiResult<Unit> {
        if (uid == null) return ApiResult.Error("No user id provided")
        if (!isOnline(context)) return ApiResult.NoInternetError()

        return try {
            with(data) {
                collection.whereEqualTo("username", username).get().also {
                    it.await()
                    if (!it.result.isEmpty && it.result.documents.first().id != uid) return ApiResult.Error("Username already taken")
                }
                val newData = mutableMapOf<String, Any>(
                    "email" to email,
                    "username" to username,
                    "firstName" to firstName,
                    "lastName" to lastName
                )
                if (avatar.loadedFromServer() || avatar.isEmpty()) newData["avatar"] = avatar
                collection.document(uid).update(newData).execute()
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun deleteUser(uid: String?): ApiResult<Unit> {
        if (uid == null) return ApiResult.Error("No user id provided")
        if (!isOnline(context)) return ApiResult.NoInternetError()

        return try {
            collection.document(uid).delete().execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    companion object {
        private const val COLLECTION_ID = "users"
    }
}