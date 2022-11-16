package com.app.testik.presentation.model

import com.app.testik.util.delegateadapter.DelegateAdapterItem
import com.app.testik.util.randomId

data class ErrorItem(
    val message: String
) : DelegateAdapterItem {

    private val id = randomId

    override fun id(): Any = id

    override fun content(): Any = this
}