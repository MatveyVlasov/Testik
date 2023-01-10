package com.app.testik.domain.model

data class GradesModel(
    val isEnabled: Boolean = false,
    val grades: List<GradeModel> = emptyList()
)