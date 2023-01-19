package com.app.testik.util

import com.app.testik.R

object Constants {

    const val DYNAMIC_LINKS_PREFIX = "https://testik.page.link"
    const val APP_NOT_INSTALLED_LINK = "https://testik.com"

    val LANGUAGES = mapOf("en" to R.string.english, "ru" to R.string.russian)

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

    const val UPDATE_QUESTION_LIST_RESULT_KEY = "UPDATE_QUESTION_LIST_RESULT_KEY"
    const val UPDATE_QUESTION_RESULT_KEY = "UPDATE_QUESTION_RESULT_KEY"
    const val DELETE_QUESTION_RESULT_KEY = "DELETE_QUESTION_RESULT_KEY"

    const val QUERY_LIMIT = 20L
}