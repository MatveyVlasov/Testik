package com.app.testik.domain.repository

import com.app.testik.data.model.*
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source

interface TestRepository {

    suspend fun createTest(testId: String, data: TestDto): ApiResult<TestCreationDto>

    suspend fun updateTest(data: TestDto): ApiResult<Unit>

    suspend fun getTestsByAuthor(uid: String?, limit: Long, snapshot: QuerySnapshot? = null): ApiResult<TestsDto>

    suspend fun getTestsByCategory(category: String, limit: Long, snapshot: QuerySnapshot? = null): ApiResult<TestsDto>

    suspend fun getTest(testId: String, source: Source): ApiResult<TestDto>

    suspend fun deleteTest(testId: String): ApiResult<Unit>

    suspend fun updateQuestions(testId: String, data: TestQuestionsDto): ApiResult<Unit>

    suspend fun getTestQuestions(testId: String): ApiResult<TestQuestionsDto>

    suspend fun updateGrades(testId: String, data: GradesDto): ApiResult<Unit>

    suspend fun getTestGrades(testId: String): ApiResult<GradesDto>
}