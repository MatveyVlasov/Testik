package com.app.testik.presentation.screen.main.model

import android.os.Parcelable
import com.app.testik.domain.model.CategoryType
import com.app.testik.util.delegateadapter.DelegateAdapterItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryTestsDelegateItem(
    val category: CategoryType,
    val tests: List<TestDelegateItem> = emptyList()
) : DelegateAdapterItem, Parcelable {

    override fun id() = category

    override fun content(): Any = this
}


