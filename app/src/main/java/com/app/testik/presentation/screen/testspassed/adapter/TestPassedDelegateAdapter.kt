package com.app.testik.presentation.screen.testspassed.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemTestPassedBinding
import com.app.testik.presentation.screen.testspassed.model.TestPassedDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter
import com.app.testik.util.loadTestImage

class TestPassedDelegateAdapter(
    val onClick: (String) -> Unit
) : DelegateAdapter<TestPassedDelegateItem, TestPassedDelegateAdapter.ViewHolder>(
    TestPassedDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemTestPassedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: TestPassedDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemTestPassedBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(test: TestPassedDelegateItem) {

            binding.apply {
                loadTestImage(context = root.context, imageView = binding.ivImage, url = test.image)

                tvTitle.text = test.title

                root.setOnClickListener { onClick(test.recordId) }
            }
        }
    }

}



