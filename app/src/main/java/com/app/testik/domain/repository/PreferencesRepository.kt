package com.app.testik.domain.repository

interface PreferencesRepository {

    fun setLanguage(lang: String)
    fun getLanguage(): String

    fun setLastUpdatedTime()
    fun getLastUpdatedTime(): Long

    fun clear()
}