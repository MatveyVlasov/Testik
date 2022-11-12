package com.app.tests.domain.repository

interface PreferencesRepository {

    fun setLanguage(lang: String)
    fun getLanguage(): String

    fun clear()
}