package com.app.testik.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.app.testik.data.model.ApiResult
import com.app.testik.domain.repository.StorageRepository
import com.app.testik.util.execute
import com.app.testik.util.isOnline
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseStorage: FirebaseStorage
) : StorageRepository {

    override suspend fun uploadAvatar(email: String, image: String): ApiResult<Uri> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        return try {
            val uri = File(image).toUri()
            val avatar = firebaseStorage.reference.child("avatars").child(email)
                .putFile(uri).await()
                .storage.downloadUrl.await()
            ApiResult.Success(avatar)
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun deleteAvatar(email: String): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

         return try {
            firebaseStorage.reference.child("avatars").child(email).delete().execute()
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    override suspend fun uploadTestImage(testId: String, image: String): ApiResult<Uri> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        return try {
            val uri = File(image).toUri()
            val testImage = firebaseStorage.reference.child("tests").child(testId).child(testId)
                .putFile(uri).await()
                .storage.downloadUrl.await()
            ApiResult.Success(testImage)
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun deleteTestImage(testId: String): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        return try {
            firebaseStorage.reference.child("tests").child(testId).child(testId).delete().execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun deleteTest(testId: String): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        return try {
            val images = firebaseStorage.reference.child("tests").child(testId).listAll().await().items
            for (image in images) {
                image.delete().await()
            }
            return ApiResult.Success()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun uploadQuestionImage(testId: String, questionId: String, image: String): ApiResult<Uri> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        return try {
            val uri = File(image).toUri()
            val testImage = firebaseStorage.reference.child("tests").child(testId).child(questionId)
                .putFile(uri).await()
                .storage.downloadUrl.await()
            ApiResult.Success(testImage)
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }
}