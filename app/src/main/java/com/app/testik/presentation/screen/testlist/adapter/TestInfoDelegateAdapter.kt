package com.app.testik.presentation.screen.testlist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemTestInfoBinding
import com.app.testik.presentation.screen.testlist.model.TestInfoDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter
import com.app.testik.util.loadTestImage

class TestInfoDelegateAdapter(
    val onClick: (String) -> Unit
) : DelegateAdapter<TestInfoDelegateItem, TestInfoDelegateAdapter.ViewHolder>(
    TestInfoDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemTestInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: TestInfoDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemTestInfoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(test: TestInfoDelegateItem) {

            binding.apply {
                loadTestImage(context = root.context, imageView = binding.ivImage, url = test.image)

                tvTitle.text = test.title
                tvCategory.setText(test.category)

                ivClosed.isVisible = !test.isOpen
                ivPassword.isVisible = test.isOpen && test.isPasswordEnabled

                root.setOnClickListener { onClick(test.id) }
            }
        }
    }

}



