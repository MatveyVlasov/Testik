package com.app.testik.domain.model

import com.google.firebase.firestore.QuerySnapshot

data class TestsModel(
    val snapshot: QuerySnapshot?,
    val tests: List<TestModel>
)