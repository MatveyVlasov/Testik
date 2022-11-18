package com.app.testik.presentation.screen.createdtests.model

import com.app.testik.util.delegateadapter.DelegateAdapterItem
import com.app.testik.util.randomId

data class CreatedTestDelegateItem(
    val id: String,
    val title: String,
    val image: String
) : DelegateAdapterItem {

    override fun id() = randomId

    override fun content(): Any = this
}


