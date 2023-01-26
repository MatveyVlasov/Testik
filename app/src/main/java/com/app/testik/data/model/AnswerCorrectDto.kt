package com.app.testik.data.model

import com.google.firebase.firestore.PropertyName

data class AnswerCorrectDto(
    val text: String = "",
    val textMatching: String = "",
    @get:PropertyName("isCorrect")
    val isCorrect: Boolean = false
)