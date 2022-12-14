package com.app.testik.presentation.screen.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemTestBinding
import com.app.testik.presentation.screen.main.model.TestDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter
import com.app.testik.util.loadTestImage

class TestDelegateAdapter(
    val onClick: (String) -> Unit
) : DelegateAdapter<TestDelegateItem, TestDelegateAdapter.ViewHolder>(
        TestDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemTestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: TestDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemTestBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(test: TestDelegateItem) {

            binding.apply {
                loadTestImage(context = root.context, imageView = binding.ivImage, url = test.image)

                tvTitle.text = test.title

                root.setOnClickListener { onClick(test.id) }
            }
        }
    }

}



