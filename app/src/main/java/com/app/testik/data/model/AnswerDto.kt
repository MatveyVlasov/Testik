package com.app.testik.data.model

import com.google.firebase.firestore.PropertyName

data class AnswerDto(
    val text: String = "",
    @get:PropertyName("isSelected")
    val isSelected: Boolean = false
)