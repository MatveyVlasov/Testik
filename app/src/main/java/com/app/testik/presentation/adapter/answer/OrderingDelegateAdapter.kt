package com.app.testik.presentation.adapter.answer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemOrderingBinding
import com.app.testik.presentation.model.answer.OrderingDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter

class OrderingDelegateAdapter(
    val isReviewMode: () -> Boolean
) : DelegateAdapter<OrderingDelegateItem, OrderingDelegateAdapter.ViewHolder>(
    OrderingDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemOrderingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: OrderingDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemOrderingBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(answer: OrderingDelegateItem) {

            binding.apply {
                tvText.text = answer.text
                tvText.isEnabled = !isReviewMode()
                tvText.isActivated = answer.text == answer.textCorrect

                tvTextCorrect.text = answer.textCorrect
                tvTextCorrect.isVisible = isReviewMode() && answer.text != answer.textCorrect

                ivMove.isVisible = !isReviewMode()
            }
        }
    }

}



