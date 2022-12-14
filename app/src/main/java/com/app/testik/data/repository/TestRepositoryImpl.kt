package com.app.testik.data.repository

import android.content.Context
import com.app.testik.data.model.*
import com.app.testik.data.model.TestDto
import com.app.testik.domain.repository.TestRepository
import com.app.testik.util.execute
import com.app.testik.util.isOnline
import com.app.testik.util.private
import com.app.testik.util.timestamp
import com.google.firebase.firestore.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TestRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseFirestore: FirebaseFirestore
): TestRepository {

    private val collection
        get() = firebaseFirestore.collection(COLLECTION_ID)

    override suspend fun createTest(data: TestDto): ApiResult<String> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        try {
            with (data) {
                val newData = mapOf(
                    "author" to author,
                    "title" to title,
                    "description" to description,
                    "category" to category,
                    "lastUpdated" to timestamp
                )

                collection.add(newData).also {
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
            collection.document(testId).update(data).execute()
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
                    "image" to image,
                    "isPublished" to isPublished,
                    "lastUpdated" to timestamp
                )

                collection.document(data.id).update(newData).execute()
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun getTestsByAuthor(uid: String?, limit: Long, snapshot: QuerySnapshot?): ApiResult<TestsDto> {
        if (uid == null) return ApiResult.Error("No user id provided")

        try {
            var query = collection
                .whereEqualTo("author", uid)
                .orderBy("lastUpdated", Query.Direction.DESCENDING)

            if (snapshot != null)
                query = query.startAfter(snapshot.orderBy("lastUpdated"))

            query
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

    override suspend fun getTestsByCategory(category: String, limit: Long, snapshot: QuerySnapshot?): ApiResult<TestsDto> {
        try {
            var query = collection
                .whereEqualTo("category", category)
                .whereEqualTo("isPublished", true)

            if (snapshot != null)
                query = query.startAfter(snapshot)

            query
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

    override suspend fun getTest(testId: String, source: Source): ApiResult<TestDto> {
        if (testId.isEmpty()) return ApiResult.Error("No test found")

        try {
            collection.document(testId).get(source).also {
                it.await()
                val test = it.result.toObject(TestDto::class.java)?.copy(id = testId)
                return if (it.isSuccessful) ApiResult.Success(test)
                else ApiResult.Error(it.exception?.message)
            }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    override suspend fun deleteTest(testId: String): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()
        if (testId.isEmpty()) return ApiResult.Error("No test found")

        return try {
            collection.document(testId).delete().execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun updateQuestions(testId: String, data: TestQuestionsDto): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()
        if (testId.isEmpty()) return ApiResult.Error("No test found")

        return try {
            var pointsMax = 0
            data.questions.forEach {
                pointsMax += it.pointsMax
            }
            val newData = mapOf(
                "questionsNum" to data.questions.size,
                "pointsMax" to pointsMax,
                "lastUpdated" to timestamp
            )

            val result = collection.document(testId).private.document("questions").set(data).execute()
            if (result is ApiResult.Success) collection.document(testId).update(newData).execute()
            else result
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun getTestQuestions(testId: String): ApiResult<TestQuestionsDto> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        try {
            collection.document(testId).private.document("questions").get().also {
                it.await()
                val data = it.result.toObject(TestQuestionsDto::class.java) ?: TestQuestionsDto()
                return if (it.isSuccessful) ApiResult.Success(data)
                else ApiResult.Error(it.exception?.message)
            }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    override suspend fun updateGrades(testId: String, data: GradesDto): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()
        if (testId.isEmpty()) return ApiResult.Error("No test found")

        return try {
            val newData = mapOf(
                "isGradesEnabled" to data.isEnabled,
                "grades" to data.grades,
                "lastUpdated" to timestamp
            )

            collection.document(testId).update(newData).execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun getTestGrades(testId: String): ApiResult<GradesDto> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        try {
            collection.document(testId).get().also {
                it.await()
                it.result.toObject(TestDto::class.java)?.let { result ->
                    val data = GradesDto(isEnabled = result.isGradesEnabled, grades = result.grades)
                    return ApiResult.Success(data)
                }
                return ApiResult.Error(it.exception?.message)
            }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    private fun QuerySnapshot?.orderBy(field: String) = this?.documents?.last()?.data?.get(field)

    companion object {
        private const val COLLECTION_ID = "tests"
    }
}