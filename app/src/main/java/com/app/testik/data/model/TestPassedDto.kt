package com.app.testik.data.model

import com.google.firebase.firestore.PropertyName

data class TestPassedDto(
    val recordId: String = "",
    val testId: String = "",
    val title: String = "",
    val image: String = "",
    val user: String = "",
    val timeStarted: Long = 0L,
    val timeFinished: Long = 0L,
    @get:PropertyName("isFinished")
    val isFinished: Boolean = false,
    val pointsMax: Int = 0,
    val pointsEarned: Int = 0,
    val pointsCalculated: Boolean = false,
    val gradeEarned: String = "",
    @get:PropertyName("isDemo")
    val isDemo: Boolean = false
)