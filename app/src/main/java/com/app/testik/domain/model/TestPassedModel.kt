package com.app.testik.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestPassedModel(
    val recordId: String = "",
    val testId: String = "",
    val title: String = "",
    val image: String = "",
    val user: String = "",
    val timeStarted: Long = 0L,
    val timeFinished: Long = 0L
): Parcelable