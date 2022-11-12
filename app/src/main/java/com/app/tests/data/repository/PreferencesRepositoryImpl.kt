package com.app.tests.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.app.tests.domain.repository.PreferencesRepository
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : PreferencesRepository {

    override fun setLanguage(lang: String) = sharedPreferences.edit {
        putString(LANGUAGE_KEY, lang)
    }

    override fun getLanguage(): String = sharedPreferences.getString(LANGUAGE_KEY, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE

    override fun clear() = sharedPreferences.edit {
        clear()
    }

    companion object {
        private const val LANGUAGE_KEY = "language"
        private const val DEFAULT_LANGUAGE = "en"
    }

}