package com.app.testik.domain.usecase

import com.app.testik.domain.repository.PreferencesRepository
import javax.inject.Inject

class PreferencesUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) {

    fun setLanguage(lang: String) = preferencesRepository.setLanguage(lang)

    fun getLanguage() = preferencesRepository.getLanguage()

    fun clear() = preferencesRepository.clear()
}
