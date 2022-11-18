package com.app.testik.data.repository

import android.content.Context
import com.app.testik.data.model.ApiResult
import com.app.testik.data.model.TestDto
import com.app.testik.data.model.TestsDto
import com.app.testik.domain.repository.TestRepository
import com.app.testik.util.execute
import com.app.testik.util.isOnline
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TestRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseFirestore: FirebaseFirestore
): TestRepository {

    override suspend fun createTest(data: TestDto): ApiResult<String> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        try {
            with (data) {
                val newData = mapOf(
                    "author" to author,
                    "title" to title,
                    "description" to description,
                    "category" to category
                )

                firebaseFirestore.collection("tests").add(newData).also {
                    it.await()
                    return if (it.isSuccessful) ApiResult.Success(it.result.id)
                    else ApiResult.Error(it.exception?.message)
                }
            }

        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    override suspend fun updateTestImage(testId: String, image: String): ApiResult<Unit> {
        if (testId.isEmpty()) return ApiResult.Error("No test found")

        return try {
            val data = mapOf("image" to image)
            firebaseFirestore.collection("tests").document(testId).update(data).execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun updateTest(data: TestDto): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()
        if (data.id.isEmpty()) return ApiResult.Error("No test found")

        return try {
            with (data) {
                val newData = mapOf(
                    "title" to title,
                    "description" to description,
                    "category" to category,
                    "image" to image
                )

                firebaseFirestore.collection("tests").document(data.id).update(newData).execute()
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun getTestsByAuthor(authorEmail: String?, limit: Long, snapshot: QuerySnapshot?): ApiResult<TestsDto> {
        if (authorEmail == null) return ApiResult.Error("No email provided")

        try {
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

    override suspend fun getTest(testId: String): ApiResult<TestDto> {
        if (testId.isEmpty()) return ApiResult.Error("No test found")

        try {
            firebaseFirestore.collection("tests").document(testId).get().also {
                it.await()
                val test = it.result.toObject(TestDto::class.java)?.copy(id = testId)
                return if (it.isSuccessful) ApiResult.Success(test)
                else ApiResult.Error(it.exception?.message)
            }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    private fun QuerySnapshot?.orderBy(field: String) = this?.documents?.last()?.data?.get(field)
}