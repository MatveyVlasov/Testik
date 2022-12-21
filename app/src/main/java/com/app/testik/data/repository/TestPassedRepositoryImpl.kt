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

    private val collection
        get() = firebaseFirestore.collection(COLLECTION_ID)

    override suspend fun createTest(data: TestPassedDto): ApiResult<TestPassedDto> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        try {
            with (data) {
                val currentTime = timestamp
                val newData = mapOf(
                    "testId" to testId,
                    "title" to title,
                    "image" to image,
                    "user" to user,
                    "timeStarted" to currentTime,
                    "timeFinished" to currentTime,
                    "pointsMax" to pointsMax
                )

                collection.add(newData).also {
                    it.await()
                    return if (it.isSuccessful) ApiResult.Success(
                        data.copy(recordId = it.result.id, timeFinished = currentTime)
                    )
                    else ApiResult.Error(it.exception?.message)
                }
            }

        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    override suspend fun finishTest(recordId: String, questions: List<QuestionDto>): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()
        if (recordId.isEmpty()) return ApiResult.Error("No test found")

        return try {
            val (pointsEarned, pointsMax) = calculatePoints(questions)

            val newData = mapOf(
                "timeFinished" to timestamp,
                "isFinished" to true,
                "questions" to questions,
                "pointsMax" to pointsMax,
                "pointsEarned" to pointsEarned
            )

            collection.document(recordId).update(newData).execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun getTestsByUser(uid: String?, limit: Long, snapshot: QuerySnapshot?): ApiResult<TestsPassedDto> {
        if (uid == null) return ApiResult.Error("No user id provided")

        try {
            var query = collection
                .whereEqualTo("user", uid)
                .orderBy("timeFinished", Query.Direction.DESCENDING)

            if (snapshot != null)
                query = query.startAfter(snapshot.orderBy("timeFinished"))

            query
                .limit(limit)
                .get()
                .also {
                    val newSnapshot = it.await()
                    return if (it.isSuccessful) ApiResult.Success(TestsPassedDto(newSnapshot))
                    else ApiResult.Error(it.exception?.message)
                }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    override suspend fun getTest(recordId: String, source: Source): ApiResult<TestPassedDto> {
        if (recordId.isEmpty()) return ApiResult.Error("No test found")

        try {
            collection.document(recordId).get(source).also {
                it.await()
                val test = it.result.toObject(TestPassedDto::class.java)?.copy(recordId = recordId)
                return if (it.isSuccessful) ApiResult.Success(test)
                else ApiResult.Error(it.exception?.message)
            }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    override suspend fun deleteTest(recordId: String): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()
        if (recordId.isEmpty()) return ApiResult.Error("No test found")

        return try {
            collection.document(recordId).delete().execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun updateQuestions(recordId: String, questions: List<QuestionDto>): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()
        if (recordId.isEmpty()) return ApiResult.Error("No test found")

        return try {
            val data = mapOf("questions" to questions, "timeFinished" to timestamp)
            collection.document(recordId).update(data).execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun calculatePoints(recordId: String, questions: List<QuestionDto>): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()
        if (recordId.isEmpty()) return ApiResult.Error("No test found")

        return try {
            val (pointsEarned, pointsMax) = calculatePoints(questions)

            val newData = mapOf(
                "questions" to questions,
                "pointsMax" to pointsMax,
                "pointsEarned" to pointsEarned,
                "pointsCalculated" to true
            )

            collection.document(recordId).update(newData).execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun getTestQuestions(recordId: String): ApiResult<List<QuestionDto>> {
        if (!isOnline(context)) return ApiResult.NoInternetError()
        if (recordId.isEmpty()) return ApiResult.Error("No test found")

        try {
            collection.document(recordId).get().also {
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

    private fun calculatePoints(questions: List<QuestionDto>): Pair<Int, Int> {
        var pointsMax = 0
        var pointsEarned = 0

        questions.forEach { question ->
            pointsMax += question.points

            var isCorrect = true
            question.answers.forEach {
                isCorrect = isCorrect && it.isCorrect == it.isSelected
            }
            if (isCorrect) pointsEarned += question.points
        }
        return Pair(pointsEarned, pointsMax)
    }

    companion object {
        private const val COLLECTION_ID = "testsPassed"
    }
}