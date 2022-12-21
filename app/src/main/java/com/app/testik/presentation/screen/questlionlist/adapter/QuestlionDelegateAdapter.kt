package com.app.testik.presentation.screen.questlionlist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.R
import com.app.testik.databinding.ItemQuestionBinding
import com.app.testik.presentation.model.QuestionDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter

class QuestionDelegateAdapter(
    val onClick: (QuestionDelegateItem) -> Unit,
    val onDeleteClick: (QuestionDelegateItem) -> Unit
) : DelegateAdapter<QuestionDelegateItem, QuestionDelegateAdapter.ViewHolder>(
        QuestionDelegateItem::class.java
    ) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: QuestionDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemQuestionBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(test: QuestionDelegateItem) {

            binding.apply {
                tvPointsData.text = test.points.toString()
                tvPoints.text = root.resources.getQuantityString(R.plurals.points_quantity, test.points)
                tvTitle.text = test.title

                root.setOnClickListener { onClick(test) }

                ivDelete.setOnClickListener { onDeleteClick(test) }
            }
        }
    }

}



