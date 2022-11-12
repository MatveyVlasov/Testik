package com.app.tests.domain.usecase

import com.app.tests.domain.repository.PreferencesRepository
import javax.inject.Inject

class PreferencesUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) {

    fun setLanguage(lang: String) = preferencesRepository.setLanguage(lang)

    fun getLanguage() = preferencesRepository.getLanguage()

    fun clear() = preferencesRepository.clear()
}
