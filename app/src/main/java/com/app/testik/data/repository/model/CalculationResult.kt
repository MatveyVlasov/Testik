package com.app.testik.data.repository.model

import com.app.testik.data.model.QuestionDto

data class CalculationResult(
    val questions: List<QuestionDto>,
    val pointsMax: Int,
    val pointsEarned: Int
)