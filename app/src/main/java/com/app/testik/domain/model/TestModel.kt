package com.app.testik.domain.model

import java.io.Serializable

data class TestModel(
    val id: String = "",
    val author: String = "",
    val title: String = "",
    val description: String = "",
    val category: CategoryType = CategoryType.NOT_SELECTED,
    val image: String = "",
    val isPublished: Boolean = false,
    val questionsNum: Int = 0
) : Serializable