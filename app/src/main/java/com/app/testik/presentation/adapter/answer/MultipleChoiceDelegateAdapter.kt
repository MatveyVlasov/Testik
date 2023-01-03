package com.app.testik.presentation.adapter.answer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.R
import com.app.testik.databinding.ItemMultipleChoiceBinding
import com.app.testik.presentation.model.answer.MultipleChoiceDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter
import com.app.testik.util.toABC

class MultipleChoiceDelegateAdapter(
    val onSelectClick: (MultipleChoiceDelegateItem, Boolean) -> Unit,
    val isReviewMode: Boolean
) : DelegateAdapter<MultipleChoiceDelegateItem, MultipleChoiceDelegateAdapter.ViewHolder>(
    MultipleChoiceDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemMultipleChoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: MultipleChoiceDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemMultipleChoiceBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(answer: MultipleChoiceDelegateItem) {

            binding.apply {
                etAnswer.setText(answer.text)

                btnSelect.isEnabled = !isReviewMode
                btnSelect.setOnCheckedChangeListener(null)
                btnSelect.isChecked = answer.isSelected
                btnSelect.setOnCheckedChangeListener { _, isChecked ->
                    onSelectClick(answer, isChecked)
                }

                tilAnswer.hint = root.context.getString(R.string.answer_option_abc, absoluteAdapterPosition.toABC())
                etAnswer.isFocusable = false
                ivDelete.isVisible = false

                ivCorrect.isVisible = isReviewMode && answer.isCorrect
                ivWrong.visibility = when {
                    isReviewMode && !answer.isCorrect && answer.isSelected -> View.VISIBLE
                    isReviewMode && !answer.isCorrect && !answer.isSelected -> View.INVISIBLE
                    else -> View.GONE
                }
            }
        }
    }
}



