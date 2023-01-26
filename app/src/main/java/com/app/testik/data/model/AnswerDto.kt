package com.app.testik.data.model

import com.google.firebase.firestore.PropertyName

data class AnswerDto(
    val text: String = "",
    val textMatching: String = "",
    @get:PropertyName("isSelected")
    val isSelected: Boolean = false
)