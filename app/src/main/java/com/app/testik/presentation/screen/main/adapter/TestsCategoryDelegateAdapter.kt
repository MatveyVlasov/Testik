package com.app.testik.presentation.screen.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemTestsCategoryBinding
import com.app.testik.domain.model.CategoryType
import com.app.testik.presentation.screen.main.model.TestsCategoryDelegateItem
import com.app.testik.util.delegateadapter.CompositeAdapter
import com.app.testik.util.delegateadapter.DelegateAdapter

class TestsCategoryDelegateAdapter(
    val onTestClick: (String) -> Unit,
    val onMoreClick: (CategoryType) -> Unit
) : DelegateAdapter<TestsCategoryDelegateItem, TestsCategoryDelegateAdapter.ViewHolder>(
        TestsCategoryDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemTestsCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: TestsCategoryDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemTestsCategoryBinding) : RecyclerView.ViewHolder(binding.root) {

        private val testsAdapter by lazy {
            CompositeAdapter.Builder()
                .add(
                    TestDelegateAdapter(onTestClick)
                )
                .build()
        }

        init {
            binding.rvTests.adapter = testsAdapter
        }

        fun bind(categoryTests: TestsCategoryDelegateItem) {

            testsAdapter.submitList(categoryTests.tests)

            binding.apply {
                tvCategoryTitle.setText(categoryTests.category.description)

                ivMore.setOnClickListener { onMoreClick(categoryTests.category) }
            }
        }
    }

}



