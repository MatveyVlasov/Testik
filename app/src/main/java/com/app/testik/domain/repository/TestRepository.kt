package com.app.testik.domain.repository

import com.app.testik.data.model.ApiResult
import com.app.testik.data.model.TestsDto
import com.google.firebase.firestore.QuerySnapshot

interface TestRepository {

    suspend fun getTestsByAuthor(authorEmail: String?, limit: Long, snapshot: QuerySnapshot? = null): ApiResult<TestsDto>
}