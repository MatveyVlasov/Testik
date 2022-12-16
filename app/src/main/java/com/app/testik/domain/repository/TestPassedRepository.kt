package com.app.testik.domain.repository

import com.app.testik.data.model.*
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source

interface TestPassedRepository {

    suspend fun createTest(data: TestPassedDto): ApiResult<String>

    suspend fun updateTest(data: TestPassedDto): ApiResult<Unit>

    suspend fun getTestsByUser(email: String?, limit: Long, snapshot: QuerySnapshot? = null): ApiResult<TestsDto>

    suspend fun getTest(testId: String, source: Source): ApiResult<TestPassedDto>

    suspend fun deleteTest(testId: String): ApiResult<Unit>

    suspend fun updateQuestions(testId: String, questions: List<QuestionDto>): ApiResult<Unit>

    suspend fun getTestQuestions(testId: String): ApiResult<List<QuestionDto>>
}