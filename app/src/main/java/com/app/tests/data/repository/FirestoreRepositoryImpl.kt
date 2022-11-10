package com.app.tests.data.repository

import com.app.tests.data.model.ApiResult
import com.app.tests.data.model.RegistrationDto
import com.app.tests.data.model.UserDto
import com.app.tests.domain.repository.FirestoreRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
): FirestoreRepository {

    override suspend fun addUser(data: RegistrationDto): ApiResult<Unit> {
        try {
            with(data) {
                val userData = mapOf(
                    "email" to email,
                    "username" to username,
                    "avatar" to avatar
                )
                firebaseFirestore.collection("users").whereEqualTo("username", username).get().also {
                    it.await()
                    if (!it.result.isEmpty) return ApiResult.Error("Username already taken")
                }
                firebaseFirestore.collection("users").document(email).set(userData).also {
                    it.await()
                    return if (it.isSuccessful) ApiResult.Success()
                    else ApiResult.Error(it.exception?.message)
                }
            }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
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
        try {
            with(data) {
                val userData = mapOf(
                    "email" to email,
                    "username" to username,
                    "avatar" to avatar
                )
                firebaseFirestore.collection("users").document(email).set(userData).also {
                    it.await()
                    return if (it.isSuccessful) ApiResult.Success()
                    else ApiResult.Error(it.exception?.message)
                }
            }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }
}