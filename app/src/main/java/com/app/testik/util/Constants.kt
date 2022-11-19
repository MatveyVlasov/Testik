package com.app.testik.util

import com.app.testik.R

object Constants {

    val LANGUAGES = mapOf("en" to R.string.english, "ru" to R.string.russian)
    val CATEGORIES = mapOf(
        "math" to R.string.category_math,
        "history" to R.string.category_history,
        "other" to R.string.category_other
    )

    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_DESCRIPTION_LENGTH = 1000
    const val USERNAME_GOOGLE_DELIMITER = '#'
    const val USERNAME_GOOGLE_ID_LENGTH = 4

    const val EXTRA_IMAGE_TITLE = "com.app.testik.extras.EXTRA_IMAGE_TITLE"
    const val EXTRA_IMAGE_PATH = "com.app.testik.extras.EXTRA_IMAGE_PATH"
    const val EXTRA_IMAGE_CROPPED_PATH = "com.app.testik.extras.EXTRA_IMAGE_CROPPED_PATH"

    const val UPDATE_AVATAR_RESULT_KEY = "UPDATE_AVATAR_RESULT_KEY"
    const val PASSWORD_CHANGED_RESULT_KEY = "PASSWORD_CHANGED_RESULT_KEY"

    const val UPDATE_TEST_RESULT_KEY = "UPDATE_TEST_RESULT_KEY"
    const val DELETE_TEST_RESULT_KEY = "DELETE_TEST_RESULT_KEY"

    const val QUERY_LIMIT = 20L
}