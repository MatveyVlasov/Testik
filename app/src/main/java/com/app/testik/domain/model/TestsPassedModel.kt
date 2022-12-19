package com.app.testik.domain.model

import com.google.firebase.firestore.QuerySnapshot

data class TestsPassedModel(
    val snapshot: QuerySnapshot?,
    val tests: List<TestPassedModel>
)