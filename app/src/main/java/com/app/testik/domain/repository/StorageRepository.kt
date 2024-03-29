package com.app.testik.domain.repository

import com.app.testik.data.model.ApiResult

interface StorageRepository {

    suspend fun uploadAvatar(uid: String, image: String): ApiResult<String>

    suspend fun deleteAvatar(uid: String): ApiResult<Unit>

    suspend fun uploadTestImage(testId: String, image: String): ApiResult<String>

    suspend fun deleteTestImage(testId: String): ApiResult<Unit>

    suspend fun deleteTest(testId: String): ApiResult<Unit>

    suspend fun uploadQuestionImage(testId: String, questionId: String, image: String): ApiResult<String>
}