package com.app.testik.presentation.screen.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemCategoryTestsBinding
import com.app.testik.presentation.screen.main.model.CategoryTestsDelegateItem
import com.app.testik.util.delegateadapter.CompositeAdapter
import com.app.testik.util.delegateadapter.DelegateAdapter

class CategoryTestsDelegateAdapter(
    val onTestClick: (String) -> Unit
    //val onMoreClick: (String) -> Unit
) : DelegateAdapter<CategoryTestsDelegateItem, CategoryTestsDelegateAdapter.ViewHolder>(
        CategoryTestsDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemCategoryTestsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: CategoryTestsDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemCategoryTestsBinding) : RecyclerView.ViewHolder(binding.root) {

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

        fun bind(categoryTests: CategoryTestsDelegateItem) {

            testsAdapter.submitList(categoryTests.tests)

            binding.apply {
                tvCategoryTitle.setText(categoryTests.category.description)
            }
        }
    }

}



