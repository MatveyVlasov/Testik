package com.app.testik.data.repository

import android.content.Context
import com.app.testik.data.model.ApiResult
import com.app.testik.data.model.TestsDto
import com.app.testik.domain.repository.TestRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TestRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseFirestore: FirebaseFirestore
): TestRepository {

    override suspend fun getTestsByAuthor(authorEmail: String?, limit: Long, snapshot: QuerySnapshot?): ApiResult<TestsDto> {
        try {
            if (authorEmail == null) return ApiResult.Error("No email provided")

            firebaseFirestore.collection("tests").whereEqualTo("author", authorEmail)
                .orderBy("title")
                .startAfter(snapshot.orderBy("title"))
                .limit(limit)
                .get()
                .also {
                    val newSnapshot = it.await()
                    return if (it.isSuccessful) ApiResult.Success(TestsDto(newSnapshot))
                    else ApiResult.Error(it.exception?.message)
                }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    private fun QuerySnapshot?.orderBy(field: String) = this?.documents?.last()?.data?.get(field)
}