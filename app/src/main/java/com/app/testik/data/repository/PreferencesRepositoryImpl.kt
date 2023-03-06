package com.app.testik.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.app.testik.domain.repository.PreferencesRepository
import com.app.testik.util.Constants.LANGUAGES
import com.app.testik.util.orZero
import com.app.testik.util.timestamp
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : PreferencesRepository {

    override fun setLanguage(lang: String) = sharedPreferences.edit {
        if (lang in LANGUAGES.keys) {
            putString(LANGUAGE_KEY, lang)
        } else {
            putString(LANGUAGE_KEY, DEFAULT_LANGUAGE)
        }
    }

    override fun getLanguage(): String = sharedPreferences.getString(LANGUAGE_KEY,"").orEmpty()

    override fun setLastUpdatedTime() = sharedPreferences.edit {
        putLong(LAST_UPDATED_KEY, timestamp)
    }

    override fun getLastUpdatedTime(): Long = sharedPreferences.getLong(LAST_UPDATED_KEY, 0L).orZero()

    override fun clear() = sharedPreferences.edit {
        clear()
    }

    companion object {
        private const val LANGUAGE_KEY = "language"
        private const val DEFAULT_LANGUAGE = "en"

        private const val LAST_UPDATED_KEY = "last_updated"
    }

}