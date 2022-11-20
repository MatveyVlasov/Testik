package com.app.testik.domain.model

import java.io.Serializable

data class QuestionModel(
    val id: String = "",
    val testId: String = "",
    val title: String = "",
    val description: String = "",
    val image: String = ""
) : Serializable