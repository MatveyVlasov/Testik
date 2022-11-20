package com.app.testik.presentation.screen.questlionlist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemQuestionBinding
import com.app.testik.presentation.screen.questlionlist.model.QuestionDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter
import com.app.testik.util.loadTestImage

class QuestionDelegateAdapter(
    val onClick: (String) -> Unit,
    val onDeleteClick: (String) -> Unit
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
                loadTestImage(context = root.context, imageView = binding.ivImage, url = test.image)

                tvTitle.text = test.title

                root.setOnClickListener { onClick(test.id) }

                ivImage.setOnClickListener { onDeleteClick(test.id) }
            }
        }
    }

}



