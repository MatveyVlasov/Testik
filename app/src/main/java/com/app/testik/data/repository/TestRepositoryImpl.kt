package com.app.testik.data.repository

import android.content.Context
import androidx.core.net.toUri
import com.app.testik.data.model.*
import com.app.testik.data.model.TestDto
import com.app.testik.domain.model.CategoryType
import com.app.testik.domain.repository.TestRepository
import com.app.testik.util.*
import com.app.testik.util.Constants.APP_NOT_INSTALLED_LINK
import com.app.testik.util.Constants.DYNAMIC_LINKS_PREFIX
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.getField
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TestRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseDynamicLinks: FirebaseDynamicLinks
): TestRepository {

    private val collection
        get() = firebaseFirestore.collection(COLLECTION_ID)

    override suspend fun createTest(testId: String, data: TestDto): ApiResult<TestCreationDto> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        try {
            with (data) {
                val link = generateTestLink(testId) ?: return ApiResult.Error("An error occurred while generating test link")

                val newData = mapOf(
                    "author" to author,
                    "title" to title,
                    "image" to image,
                    "description" to description,
                    "category" to category,
                    "isPasswordEnabled" to isPasswordEnabled,
                    "link" to link,
                    "lastUpdated" to timestamp
                )

                collection.document(testId).set(newData).also {
                    it.await()
                    return if (it.isSuccessful) ApiResult.Success(TestCreationDto(id = testId, link = link))
                    else ApiResult.Error(it.exception?.message)
                }
            }

        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    override suspend fun updateTest(data: TestDto): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()
        if (data.id.isEmpty()) return ApiResult.Error("No test found")

        return try {
            with (data) {
                val newData = mapOf(
                    "title" to title,
                    "description" to description,
                    "category" to category,
                    "image" to image,
                    "isPasswordEnabled" to isPasswordEnabled,
                    "isPublished" to isPublished,
                    "isLinkEnabled" to isLinkEnabled,
                    "link" to link,
                    "isResultsShown" to isResultsShown,
                    "isNavigationEnabled" to isNavigationEnabled,
                    "isRandomQuestions" to isRandomQuestions,
                    "isRandomAnswers" to isRandomAnswers,
                    "lastUpdated" to timestamp
                )

                collection.document(data.id).update(newData).execute()
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun getTestsByAuthor(uid: String?, limit: Long, snapshot: QuerySnapshot?): ApiResult<TestsDto> {
        if (uid == null) return ApiResult.Error("No user id provided")

        try {
            var query = collection
                .whereEqualTo("author", uid)
                .orderBy("lastUpdated", Query.Direction.DESCENDING)

            if (snapshot != null)
                query = query.startAfter(snapshot.orderBy("lastUpdated"))

            query
                .limit(limit)
                .get()
                .also {
                    val newSnapshot = it.await()
                    return if (it.isSuccessful) ApiResult.Success(TestsDto(newSnapshot))
                    else ApiResult.Error(it.exception?.message)
                }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    override suspend fun getTestsByCategory(
        category: String,
        limit: Long,
        source: Source,
        snapshot: QuerySnapshot?,
        author: String?
    ): ApiResult<TestsDto> {
        try {
            var query = collection.whereEqualTo("isPublished", true)

            if (category != CategoryType.ALL.title) query = query.whereEqualTo("category", category)
            if (author != null) query = query.whereEqualTo("author", author)

            query = query.orderBy("lastUpdated", Query.Direction.DESCENDING)

            if (snapshot != null)
                query = query.startAfter(snapshot)

            query
                .limit(limit)
                .get(source)
                .also {
                    val newSnapshot = it.await()
                    return if (it.isSuccessful) ApiResult.Success(TestsDto(newSnapshot))
                    else ApiResult.Error(it.exception?.message)
                }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    override suspend fun getTest(testId: String, source: Source): ApiResult<TestDto> {
        if (testId.isEmpty()) return ApiResult.Error("No test found")

        try {
            collection.document(testId).get(source).also {
                it.await()
                val test = it.result.toObject(TestDto::class.java)?.copy(id = testId) ?: return ApiResult.Error("Test not found")
                return if (it.isSuccessful) ApiResult.Success(test)
                else ApiResult.Error(it.exception?.message)
            }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    override suspend fun deleteTest(testId: String): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()
        if (testId.isEmpty()) return ApiResult.Error("No test found")

        return try {
            collection.document(testId).delete().execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun updateQuestions(testId: String, data: TestQuestionsDto): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()
        if (testId.isEmpty()) return ApiResult.Error("No test found")

        return try {
            var pointsMax = 0
            data.questions.forEach {
                pointsMax += it.pointsMax
            }
            val newData = mapOf(
                "questionsNum" to data.questions.size,
                "pointsMax" to pointsMax,
                "lastUpdated" to timestamp
            )

            val result = collection.document(testId).private.document("questions").set(data).execute()
            if (result is ApiResult.Success) collection.document(testId).update(newData).execute()
            else result
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun getQuestions(testId: String): ApiResult<TestQuestionsDto> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        try {
            collection.document(testId).private.document("questions").get().also {
                it.await()
                val data = it.result.toObject(TestQuestionsDto::class.java) ?: TestQuestionsDto()
                return if (it.isSuccessful) ApiResult.Success(data)
                else ApiResult.Error(it.exception?.message)
            }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    override suspend fun updateGrades(testId: String, data: GradesDto): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()
        if (testId.isEmpty()) return ApiResult.Error("No test found")

        return try {
            val newData = mapOf(
                "isGradesEnabled" to data.isEnabled,
                "grades" to data.grades,
                "lastUpdated" to timestamp
            )

            collection.document(testId).update(newData).execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun getGrades(testId: String): ApiResult<GradesDto> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        try {
            collection.document(testId).get().also {
                it.await()
                it.result.toObject(TestDto::class.java)?.let { result ->
                    val data = GradesDto(isEnabled = result.isGradesEnabled, grades = result.grades)
                    return ApiResult.Success(data)
                }
                return ApiResult.Error(it.exception?.message)
            }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    override suspend fun updatePassword(testId: String, password: String): ApiResult<Unit> {
        if (!isOnline(context)) return ApiResult.NoInternetError()
        if (testId.isEmpty()) return ApiResult.Error("No test found")

        return try {
            val newData = mapOf(
                "password" to password
            )

            collection.document(testId).private.document("password").set(newData).execute()
        } catch (e: Exception) {
            ApiResult.Error(e.message)
        }
    }

    override suspend fun getPassword(testId: String): ApiResult<String> {
        if (!isOnline(context)) return ApiResult.NoInternetError()

        try {
            collection.document(testId).private.document("password").get().also {
                it.await()
                if (it.isSuccessful) {
                    val password = it.result.getField("password") as? String
                    return ApiResult.Success(password.orEmpty())
                }
                return ApiResult.Error(it.exception?.message)
            }
        } catch (e: Exception) {
            return ApiResult.Error(e.message)
        }
    }

    private suspend fun generateTestLink(testId: String): String? {

        firebaseDynamicLinks.createDynamicLink().apply {
            link = "$DYNAMIC_LINKS_PREFIX/test/$testId".toUri()
            domainUriPrefix = DYNAMIC_LINKS_PREFIX

            androidParameters {
                fallbackUrl = APP_NOT_INSTALLED_LINK.toUri()
                build()
            }

            buildShortDynamicLink().also {
                it.await()
                if (it.isSuccessful) return it.result.shortLink.toString()
            }
        }

        return null
    }

    private fun QuerySnapshot?.orderBy(field: String) = this?.documents?.last()?.data?.get(field)

    companion object {
        private const val COLLECTION_ID = "tests"
    }
}