package com.app.testik.presentation.screen.questlionlist.model

import com.app.testik.util.delegateadapter.DelegateAdapterItem

data class QuestionDelegateItem(
    val id: String,
    val testId: String,
    val title: String,
    val description: String,
    val image: String
) : DelegateAdapterItem {

    override fun id() = id

    override fun content(): Any = this
}


