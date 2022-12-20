package com.app.testik.presentation.screen.testspassed.model

import com.app.testik.util.delegateadapter.DelegateAdapterItem

data class TestPassedDelegateItem(
    val recordId: String,
    val title: String,
    val image: String
) : DelegateAdapterItem {

    override fun id() = recordId

    override fun content(): Any = this
}

