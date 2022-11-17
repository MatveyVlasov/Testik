package com.app.testik.presentation.screen.createdtests.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemCreatedTestBinding
import com.app.testik.presentation.screen.createdtests.model.CreatedTestDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter

class CreatedTestDelegateAdapter(
    val onMoreClick: (View) -> Unit
) : DelegateAdapter<CreatedTestDelegateItem, CreatedTestDelegateAdapter.ViewHolder>(
        CreatedTestDelegateItem::class.java
    ) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemCreatedTestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: CreatedTestDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemCreatedTestBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(test: CreatedTestDelegateItem) {

            binding.apply {
                tvTitle.text = test.title

                ivMore.setOnClickListener { onMoreClick(it) }
            }
        }
    }

}



