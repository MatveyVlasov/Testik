package com.app.testik.presentation.adapter.answer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemMatchingLeftBinding
import com.app.testik.presentation.model.answer.MatchingLeftDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter

class MatchingLeftDelegateAdapter : DelegateAdapter<MatchingLeftDelegateItem, MatchingLeftDelegateAdapter.ViewHolder>(
    MatchingLeftDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemMatchingLeftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: MatchingLeftDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemMatchingLeftBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(answer: MatchingLeftDelegateItem) {

            binding.apply {
                tvText.text = answer.text
            }
        }
    }

}



