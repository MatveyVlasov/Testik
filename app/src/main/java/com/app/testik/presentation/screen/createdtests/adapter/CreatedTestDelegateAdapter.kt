package com.app.testik.presentation.screen.createdtests.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemCreatedTestBinding
import com.app.testik.presentation.screen.createdtests.model.CreatedTestDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter
import com.app.testik.util.loadTestImage

class CreatedTestDelegateAdapter(
    val onClick: (String) -> Unit,
    val onMoreClick: (View, String) -> Unit
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
                loadTestImage(context = root.context, imageView = binding.ivImage, url = test.image)

                tvTitle.text = test.title

                root.setOnClickListener { onClick(test.id) }
                ivMore.setOnClickListener { onMoreClick(it, test.id) }
            }
        }
    }

}



