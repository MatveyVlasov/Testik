package com.app.testik.data.model

import com.google.firebase.firestore.PropertyName

data class TestDto(
    val id: String = "",
    val author: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val image: String = "",
    @get:PropertyName("isPublished")
    val isPublished: Boolean = false
)