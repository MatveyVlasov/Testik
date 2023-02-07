package com.app.testik.domain.repository

import com.app.testik.data.model.*
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source

interface TestPassedRepository {

    suspend fun startTest(data: TestPassedDto, password: String): ApiResult<TestPassedDto>

    suspend fun finishTest(recordId: String, questions: List<QuestionDto>): ApiResult<Unit>

    suspend fun calculatePoints(recordId: String): ApiResult<PointsEarnedDto>

    suspend fun submitQuestion(recordId: String, question: QuestionDto, num: Int): ApiResult<AnswerResultsDto>

    suspend fun getTestsByUser(uid: String?, limit: Long, snapshot: QuerySnapshot? = null): ApiResult<TestsPassedDto>

    suspend fun getTests(testId: String, limit: Long, snapshot: QuerySnapshot? = null): ApiResult<TestsPassedDto>

    suspend fun getTest(recordId: String, source: Source): ApiResult<TestPassedDto>

    suspend fun deleteTest(recordId: String): ApiResult<Unit>

    suspend fun updateQuestions(recordId: String, questions: List<QuestionDto>): ApiResult<Unit>

    suspend fun getQuestions(recordId: String): ApiResult<List<QuestionDto>>

    suspend fun getResults(recordId: String): ApiResult<ResultsDto>
}