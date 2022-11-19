package com.app.testik.domain.model

import java.io.Serializable

data class TestModel(
    val id: String = "",
    val author: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val image: String = ""
) : Serializable