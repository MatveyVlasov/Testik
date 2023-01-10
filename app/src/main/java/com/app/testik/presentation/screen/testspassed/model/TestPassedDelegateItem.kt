package com.app.testik.presentation.screen.testspassed.model

import com.app.testik.util.delegateadapter.DelegateAdapterItem

data class TestPassedDelegateItem(
    val recordId: String,
    val title: String,
    val image: String,
    val date: String,
    val isFinished: Boolean,
    val pointsMax: Int,
    val pointsEarned: Int,
    val pointsCalculated: Boolean,
    val gradeEarned: String,
    val isDemo: Boolean
) : DelegateAdapterItem {

    override fun id() = recordId

    override fun content(): Any = this
}


