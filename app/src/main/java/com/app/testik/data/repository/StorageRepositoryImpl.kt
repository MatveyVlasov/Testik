package com.app.testik.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.app.testik.data.model.ApiResult
import com.app.testik.data.model.UserDto
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

    override suspend fun uploadAvatar(data: UserDto): ApiResult<Uri> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        return try {
            val uri = File(data.avatar).toUri()
            val avatar = firebaseStorage.reference.child("avatars").child(data.email)
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
            val testImage = firebaseStorage.reference.child("tests").child(testId)
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
            firebaseStorage.reference.child("tests").child(testId).delete().execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }
}