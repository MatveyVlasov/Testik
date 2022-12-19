package com.app.testik.data.model

import com.google.firebase.firestore.QuerySnapshot

data class TestsPassedDto(
    val snapshot: QuerySnapshot
)