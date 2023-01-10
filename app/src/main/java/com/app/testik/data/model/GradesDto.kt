package com.app.testik.data.model

data class GradesDto(
    val isEnabled: Boolean = false,
    val grades: List<GradeDto> = emptyList()
)