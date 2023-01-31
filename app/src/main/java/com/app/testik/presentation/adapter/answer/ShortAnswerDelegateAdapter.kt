package com.app.testik.presentation.adapter.answer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemShortAnswerBinding
import com.app.testik.presentation.model.answer.ShortAnswerDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter

class ShortAnswerDelegateAdapter : DelegateAdapter<ShortAnswerDelegateItem, ShortAnswerDelegateAdapter.ViewHolder>(
    ShortAnswerDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemShortAnswerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: ShortAnswerDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemShortAnswerBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(answer: ShortAnswerDelegateItem) {

            binding.apply {
                tilAnswer.isExpandedHintEnabled = false

                etAnswer.setText(answer.text)
                etAnswer.isFocusable = false

                ivDelete.isVisible = false
            }
        }
    }

}



