package com.app.testik.presentation.screen.main.model

import com.app.testik.domain.model.CategoryType
import com.app.testik.util.delegateadapter.DelegateAdapterItem
import java.io.Serializable

data class CategoryTestsDelegateItem(
    val category: CategoryType,
    val tests: List<TestDelegateItem> = emptyList()
) : DelegateAdapterItem, Serializable {

    override fun id() = category

    override fun content(): Any = this
}


