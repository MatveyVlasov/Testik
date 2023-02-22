package com.app.testik.presentation.screen.testlist.model

import androidx.annotation.StringRes
import com.app.testik.util.delegateadapter.DelegateAdapterItem

data class TestInfoDelegateItem(
    val id: String,
    val title: String,
    val image: String,
    @StringRes val category: Int
) : DelegateAdapterItem {

    override fun id() = id

    override fun content(): Any = this
}


