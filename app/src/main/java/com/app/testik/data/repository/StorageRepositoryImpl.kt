package com.app.testik.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import com.app.testik.data.model.ApiResult
import com.app.testik.domain.repository.StorageRepository
import com.app.testik.util.execute
import com.app.testik.util.isOnline
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseStorage: FirebaseStorage
) : StorageRepository {

    override suspend fun uploadAvatar(email: String, image: String): ApiResult<Uri> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        return try {
            val avatar = firebaseStorage.reference.child("avatars").child(email).uploadImage(image)
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
            val testImage = firebaseStorage.reference.child("tests").child(testId).child(testId).uploadImage(image)
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
            val testImage = firebaseStorage.reference.child("tests").child(testId).child(questionId).uploadImage(image)
            ApiResult.Success(testImage)
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    private suspend fun StorageReference.uploadImage(image: String) =
        putFile(compressImage(image).toUri()).await().storage.downloadUrl.await()

    private fun compressImage(image: String): File {
        val outputDir = context.cacheDir
        val compressedImageFile = File.createTempFile("temp", ".jpg", outputDir)

        try {
            val bitmap = BitmapFactory.decodeFile(image)
            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                IMAGE_COMPRESS_RATIO,
                FileOutputStream(compressedImageFile)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return compressedImageFile
    }

    companion object {
        const val IMAGE_COMPRESS_RATIO = 50
    }

}