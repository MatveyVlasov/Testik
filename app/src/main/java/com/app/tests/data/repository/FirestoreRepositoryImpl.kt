package com.app.tests.data.repository

import com.app.tests.data.model.ApiResult
import com.app.tests.data.model.RegistrationDto
import com.app.tests.domain.repository.FirestoreRepository
import com.google.firebase.firestore.FirebaseFirestore
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
                    "username" to username
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