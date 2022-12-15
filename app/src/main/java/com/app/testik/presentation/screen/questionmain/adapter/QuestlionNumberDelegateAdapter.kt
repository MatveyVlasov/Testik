package com.app.testik.presentation.screen.questionmain.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemQuestionNumberBinding
import com.app.testik.presentation.screen.questionmain.model.QuestionNumberDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter

class QuestionNumberDelegateAdapter(
    val onClick: (Int) -> Unit
) : DelegateAdapter<QuestionNumberDelegateItem, QuestionNumberDelegateAdapter.ViewHolder>(QuestionNumberDelegateItem::class.java) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemQuestionNumberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: QuestionNumberDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemQuestionNumberBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: QuestionNumberDelegateItem) {
            binding.tvTitle.apply {
                val displayedNumber = (item.number + 1).toString()
                text = displayedNumber
                isSelected = item.isSelected

                setOnClickListener { onClick(item.number) }
            }
        }
    }
}



