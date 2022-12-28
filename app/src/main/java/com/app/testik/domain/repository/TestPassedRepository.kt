package com.app.testik.domain.repository

import com.app.testik.data.model.*
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source

interface TestPassedRepository {

    suspend fun createTest(data: TestPassedDto): ApiResult<TestPassedDto>

    suspend fun finishTest(recordId: String, questions: List<QuestionDto>): ApiResult<Unit>

    suspend fun getTestsByUser(uid: String?, limit: Long, snapshot: QuerySnapshot? = null): ApiResult<TestsPassedDto>

    suspend fun getTest(recordId: String, source: Source): ApiResult<TestPassedDto>

    suspend fun deleteTest(recordId: String): ApiResult<Unit>

    suspend fun updateQuestions(recordId: String, questions: List<QuestionDto>): ApiResult<Unit>

    suspend fun getTestQuestions(recordId: String): ApiResult<List<QuestionDto>>

    suspend fun getTestResults(recordId: String): ApiResult<ResultsDto>
}