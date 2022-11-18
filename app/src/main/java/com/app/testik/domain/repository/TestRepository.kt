package com.app.testik.domain.repository

import com.app.testik.data.model.ApiResult
import com.app.testik.data.model.TestDto
import com.app.testik.data.model.TestsDto
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source

interface TestRepository {

    suspend fun createTest(data: TestDto): ApiResult<String>

    suspend fun updateTest(data: TestDto): ApiResult<Unit>

    suspend fun updateTestImage(testId: String, image: String): ApiResult<Unit>

    suspend fun getTestsByAuthor(authorEmail: String?, limit: Long, snapshot: QuerySnapshot? = null): ApiResult<TestsDto>

    suspend fun getTest(testId: String, source: Source): ApiResult<TestDto>

    suspend fun deleteTest(testId: String): ApiResult<Unit>
}