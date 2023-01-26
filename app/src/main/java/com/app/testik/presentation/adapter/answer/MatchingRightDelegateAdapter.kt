package com.app.testik.presentation.adapter.answer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemMatchingRightBinding
import com.app.testik.presentation.model.answer.MatchingRightDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter

class MatchingRightDelegateAdapter(
    val isReviewMode: Boolean
) : DelegateAdapter<MatchingRightDelegateItem, MatchingRightDelegateAdapter.ViewHolder>(
    MatchingRightDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemMatchingRightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: MatchingRightDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemMatchingRightBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(answer: MatchingRightDelegateItem) {

            binding.apply {
                tvText.text = answer.textMatching
                tvText.isEnabled = !isReviewMode
                tvText.isActivated = answer.textMatching == answer.textCorrect

                tvTextCorrect.text = answer.textCorrect
                tvTextCorrect.isVisible = isReviewMode && answer.textMatching != answer.textCorrect

                ivMove.isVisible = !isReviewMode
            }
        }
    }

}



