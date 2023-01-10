package com.app.testik.presentation.screen.gradingsystem.model

import com.app.testik.util.delegateadapter.DelegateAdapterItem
import com.app.testik.util.randomId

data class GradeDelegateItem(
    val id: String = randomId,
    val grade: String = "",
    val pointsFrom: String = "",
    val pointsTo: String = ""
) : DelegateAdapterItem {

    override fun id() = id

    override fun content(): Any = this
}


