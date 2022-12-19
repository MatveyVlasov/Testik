package com.app.testik.presentation.screen.testscreated.model

import com.app.testik.util.delegateadapter.DelegateAdapterItem

data class TestCreatedDelegateItem(
    val id: String,
    val title: String,
    val image: String
) : DelegateAdapterItem {

    override fun id() = id

    override fun content(): Any = this
}


