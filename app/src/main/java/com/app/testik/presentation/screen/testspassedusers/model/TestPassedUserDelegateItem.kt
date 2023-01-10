package com.app.testik.presentation.screen.testspassedusers.model

import com.app.testik.util.delegateadapter.DelegateAdapterItem

data class TestPassedUserDelegateItem(
    val recordId: String,
    val user: String,
    val title: String,
    val date: String,
    val isFinished: Boolean,
    val pointsMax: Int,
    val pointsEarned: Int,
    val pointsCalculated: Boolean,
    val gradeEarned: String,
    val username: String = "",
    val avatar: String = ""
) : DelegateAdapterItem {

    override fun id() = recordId

    override fun content(): Any = this
}


