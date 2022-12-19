package com.app.testik.presentation.screen.testscreated.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemTestCreatedBinding
import com.app.testik.presentation.screen.testscreated.model.TestCreatedDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter
import com.app.testik.util.loadTestImage

class TestCreatedDelegateAdapter(
    val onClick: (String) -> Unit,
    val onMoreClick: (View, String) -> Unit
) : DelegateAdapter<TestCreatedDelegateItem, TestCreatedDelegateAdapter.ViewHolder>(
    TestCreatedDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemTestCreatedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: TestCreatedDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemTestCreatedBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(test: TestCreatedDelegateItem) {

            binding.apply {
                loadTestImage(context = root.context, imageView = binding.ivImage, url = test.image)

                tvTitle.text = test.title

                root.setOnClickListener { onClick(test.id) }
                ivMore.setOnClickListener { onMoreClick(it, test.id) }
            }
        }
    }

}



