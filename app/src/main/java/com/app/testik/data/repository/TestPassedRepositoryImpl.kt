package com.app.testik.data.repository

import android.content.Context
import com.app.testik.data.model.*
import com.app.testik.domain.repository.TestPassedRepository
import com.app.testik.util.execute
import com.app.testik.util.isOnline
import com.app.testik.util.timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TestPassedRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseFirestore: FirebaseFirestore
): TestPassedRepository {

    override suspend fun createTest(data: TestPassedDto): ApiResult<String> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        try {
            with (data) {
                val newData = mapOf(
                    "testId" to testId,
                    "user" to user,
                    "timeStarted" to timeStarted,
                    "timeFinished" to timeFinished
                )

                firebaseFirestore.collection("testsPassed").add(newData).also {
                    it.await()
                    return if (it.isSuccessful) ApiResult.Success(it.result.id)
                    else ApiResult.Error(it.exception?.message)
                }
            }

        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    override suspend fun updateTest(data: TestPassedDto): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()
        if (data.id.isEmpty()) return ApiResult.Error("No test found")

        return try {
            with (data) {
                val newData = mapOf(
                    "testId" to testId,
                    "user" to user,
                    "timeStarted" to timeStarted,
                    "timeFinished" to timeFinished
                )

                firebaseFirestore.collection("testsPassed").document(data.id).update(newData).execute()
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun getTestsByUser(email: String?, limit: Long, snapshot: QuerySnapshot?): ApiResult<TestsDto> {
        if (email == null) return ApiResult.Error("No email provided")

        try {
            var query = firebaseFirestore.collection("testsPassed")
                .whereEqualTo("user", email)
                .orderBy("timeFinished", Query.Direction.DESCENDING)

            if (snapshot != null)
                query = query.startAfter(snapshot.orderBy("timeFinished"))

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

    override suspend fun getTest(testId: String, source: Source): ApiResult<TestPassedDto> {
        if (testId.isEmpty()) return ApiResult.Error("No test found")

        try {
            firebaseFirestore.collection("testsPassed").document(testId).get(source).also {
                it.await()
                val test = it.result.toObject(TestPassedDto::class.java)?.copy(id = testId)
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
            firebaseFirestore.collection("testsPassed").document(testId).delete().execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun updateQuestions(testId: String, questions: List<QuestionDto>): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()
        if (testId.isEmpty()) return ApiResult.Error("No test found")

        return try {
            val data = mapOf("questions" to questions, "timeFinished" to timestamp)
            firebaseFirestore.collection("testsPassed").document(testId).update(data).execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun getTestQuestions(testId: String): ApiResult<List<QuestionDto>> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        try {
            firebaseFirestore.collection("testsPassed").document(testId).get().also {
                it.await()
                val questions = it.result.toObject(TestQuestionsDto::class.java)?.questions ?: emptyList()
                return if (it.isSuccessful) ApiResult.Success(questions)
                else ApiResult.Error(it.exception?.message)
            }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    private fun QuerySnapshot?.orderBy(field: String) = this?.documents?.last()?.data?.get(field)
}