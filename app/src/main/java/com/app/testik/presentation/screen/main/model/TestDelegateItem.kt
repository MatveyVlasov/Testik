package com.app.testik.presentation.screen.main.model

import android.os.Parcelable
import com.app.testik.util.delegateadapter.DelegateAdapterItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestDelegateItem(
    val id: String,
    val title: String,
    val image: String,
    val isOpen: Boolean,
    val isPasswordEnabled: Boolean
) : DelegateAdapterItem, Parcelable {

    override fun id() = id

    override fun content(): Any = this
}


